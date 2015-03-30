package org.no_ip.chrisbregg.simon;

import android.media.AudioManager;
import android.media.SoundPool;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.view.MotionEvent;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Chris on 2015-03-02.
 */
public class SimonGLRenderer implements GLSurfaceView.Renderer, SoundPlayer.SoundPlayerLoadCompleteListener {
    private enum DRAW_STATE { DRAW_PATTERN, DRAW_BOARD, PATTERN_COMPLETE, PATTERN_FAILED, GAME_INIT };

    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    private GameBoard mBoard;
    private ArrayList<Integer> mPattern;
    private int mCurrentPatternStep = 0;
    private long mPatternStepStartTimeMillis = 0;

    private Random mRand;

    private DRAW_STATE mCurrentDrawState = DRAW_STATE.DRAW_BOARD;

    private int mWidth;
    private int mHeight;
    private float mRatio;

    private int lastToggled;
    private int patternLoc = 0; // where in the pattern are we for player playback

    private boolean gameStarted = false;

    private SoundPlayer mSoundPlayer;

    private int mGameMode;

    public SimonGLRenderer(int gameMode) {
        setGameMode(gameMode);
    }

    public static int loadShader(int type, String shaderCode) {
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    @Override
    public void OnAudioLoadComplete() {
        initPattern(); // start playing the pattern once the audio is loaded
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.2f, 0.2f, 0.2f, 1.0f);

        mSoundPlayer = new SoundPlayer();
        mSoundPlayer.setOnLoadCompleteListener(this);

        mRand = new Random();
        mBoard = new GameBoard();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) height / width;
        mRatio = ratio;

        mWidth = width;
        mHeight = height;

        Matrix.orthoM(mProjectionMatrix, 0, -1, 1, -ratio, ratio, 3, 7);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // set the camera position
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        switch (mCurrentDrawState) {
            case DRAW_BOARD:
                mBoard.draw(mMVPMatrix);
                break;

            case DRAW_PATTERN:
                try {
                    // The pattern step just started, turn the light on
                    if (mPatternStepStartTimeMillis == 0 && mCurrentPatternStep < mPattern.size()) {
                        mPatternStepStartTimeMillis = System.currentTimeMillis();

                        int lightQuadrant = mPattern.get(mCurrentPatternStep);

                        toggleQuadrant(lightQuadrant);
                    } else if ((System.currentTimeMillis() - mPatternStepStartTimeMillis) >= 750) {
                        // turn the light off
                        toggleQuadrant(mPattern.get(mCurrentPatternStep++));

                        mPatternStepStartTimeMillis = 0;

                        // if we just completed the last step in the pattern, reset to simply drawing the board
                        if (mCurrentPatternStep >= mPattern.size()) {
                            mCurrentPatternStep = 0;
                            mCurrentDrawState = DRAW_STATE.DRAW_BOARD;
                        }
                    }
                } catch (IndexOutOfBoundsException ex) {

                }

                mBoard.draw(mMVPMatrix);
                break;

            case PATTERN_COMPLETE:
                if (mPatternStepStartTimeMillis == 0) {
                    // Start a timer, but don't do anything else for a moment

                    mPatternStepStartTimeMillis = System.currentTimeMillis();
                } else if ((System.currentTimeMillis() - mPatternStepStartTimeMillis) >= 750) {
                    mPatternStepStartTimeMillis = 0; // reset the timer

                    // After a brief pause, make the pattern harder and play it
                    addRandomPatternItem();
                    playPattern();
                }

                mBoard.draw(mMVPMatrix);
                break;

            case PATTERN_FAILED:
                if (mPatternStepStartTimeMillis == 0) {
                    // Start a timer, but don't do anything else for a moment

                    mPatternStepStartTimeMillis = System.currentTimeMillis();
                } else if ((System.currentTimeMillis() - mPatternStepStartTimeMillis) >= 750) {
                    mPatternStepStartTimeMillis = 0; // reset the timer

                    // Light up all the lights so that the player knows the game is over
                    for (int x = 0; x < mBoard.getQuadrantCount(); x++) {
                        mBoard.toggleQuadrant(x);
                    }
                }

                mBoard.draw(mMVPMatrix);
                break;

            case GAME_INIT:
                init();
                mBoard.draw(mMVPMatrix);
                break;

            default:
                mBoard.draw(mMVPMatrix);
                break;
        }
    }

    // Screen was touched at x,y coord
    public void onTouchEvent(MotionEvent event) {
        // Don't acknowledge a interaction unless the game has already started
        if (!gameStarted) {
            return;
        }

        if (mCurrentDrawState == DRAW_STATE.DRAW_BOARD &&
                (event.getAction() == MotionEvent.ACTION_DOWN)) {
            float normalX = getNormalizedXCoord(event.getX());
            float normalY = getNormalizedYCoord(event.getY());

            int selectedQuadrant = mBoard.getQuadrantXY(normalX, normalY);

            lastToggled = selectedQuadrant;

            if (selectedQuadrant != -1) {
                toggleQuadrant(selectedQuadrant);
            }
        } else if (mCurrentDrawState == DRAW_STATE.DRAW_BOARD &&
                event.getAction() == MotionEvent.ACTION_UP) {
            toggleQuadrant(lastToggled);

            int comparePatternQuadrant = -1;

            if (mGameMode == MainGameActivity.GAME_MODE_CLASSIC) {
                comparePatternQuadrant = mPattern.get(patternLoc);
            } else if (mGameMode == MainGameActivity.GAME_MODE_REVERSE) {
                // adjust for 0 based array
                comparePatternQuadrant = mPattern.get(mPattern.size() - patternLoc - 1);
            }

            if (comparePatternQuadrant == -1) {
                throw new InvalidParameterException();
            }

            if (lastToggled == comparePatternQuadrant) {
                patternLoc++;
            } else if (lastToggled != -1) {
                // if a quadrant was actually selected
                mCurrentDrawState = DRAW_STATE.PATTERN_FAILED;
            }

            if (patternLoc == mPattern.size()) {
                mCurrentDrawState = DRAW_STATE.PATTERN_COMPLETE;
                patternLoc = 0;
            }
        }
    }

    public void initPattern() {
        mCurrentDrawState = DRAW_STATE.GAME_INIT;
    }

    private void init() {
        mPattern = new ArrayList<Integer>();

        patternLoc = 0;
        mCurrentPatternStep = 0;

        addRandomPatternItem();
        playPattern();

        gameStarted = true;
    }

    // Convert the given screen x-coord to normalized coords
    private float getNormalizedXCoord(float x) {
        float normalX = (x / ((float)mWidth / 2.0f)) - 1;

        return normalX;
    }// Convert the given screen x-coord to normalized coords

    private float getNormalizedYCoord(float y) {
        // Shutup code tips, this is here to make debugging easier
        float normalY = ((-y + (float)mHeight) / ((float)mWidth / 2.0f)) - mRatio;

        return normalY;
    }

    public void addRandomPatternItem() {
        addPatternItem(mRand.nextInt(mBoard.getQuadrantCount()));
    }

    public void addPatternItem(Integer quadrant) {
        if (quadrant < mBoard.getQuadrantCount()) {
            mPattern.add(quadrant);
        }
    }

    public void playPattern() {
        for (int x = 0; x < mBoard.getQuadrantCount(); x++) {
            // Make sure all the lights are off before starting the pattern
            mBoard.toggleQuadrant(x, false);
        }

        mPatternStepStartTimeMillis = 0; // initialize the pattern timer

        mCurrentDrawState = DRAW_STATE.DRAW_PATTERN;
    }

    public void toggleQuadrant(int x) {
        if (mBoard.toggleQuadrant(x)) {
            switch (x) {
                case 0:
                    mSoundPlayer.playSound(SoundPlayer.RED_TONE);
                    break;

                case 1:
                    mSoundPlayer.playSound(SoundPlayer.BLUE_TONE);
                    break;

                case 2:
                    mSoundPlayer.playSound(SoundPlayer.YELLOW_TONE);
                    break;

                case 3:
                    mSoundPlayer.playSound(SoundPlayer.GREEN_TONE);
                    break;

                default:
                    // don't play a sound
            }
        }
    }

    public void setGameMode(int gameMode) {
        mGameMode = gameMode;
    }
}
