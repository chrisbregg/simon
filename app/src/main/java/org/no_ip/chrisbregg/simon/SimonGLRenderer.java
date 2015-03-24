package org.no_ip.chrisbregg.simon;

import android.media.AudioManager;
import android.media.SoundPool;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Chris on 2015-03-02.
 */
public class SimonGLRenderer implements GLSurfaceView.Renderer {
    private enum DRAW_STATE { DRAW_PATTERN, DRAW_BOARD };

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
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);

        mBoard = new GameBoard();

        mPattern = new ArrayList<Integer>();

        mRand = new Random();

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
                // The pattern step just started, turn the light on
                if (mPatternStepStartTimeMillis == 0 && mCurrentPatternStep < mPattern.size()) {
                    mPatternStepStartTimeMillis = System.currentTimeMillis();

                    int lightQuadrant = mPattern.get(mCurrentPatternStep);

                    mBoard.toggleQuadrant(lightQuadrant);
                } else if ((System.currentTimeMillis() - mPatternStepStartTimeMillis) >= 750) {
                    // turn the light off
                    mBoard.toggleQuadrant(mPattern.get(mCurrentPatternStep++));

                    mPatternStepStartTimeMillis = 0;

                    // if we just completed the last step in the pattern, reset to simply drawing the board
                    if (mCurrentPatternStep >= mPattern.size()) {
                        mCurrentPatternStep = 0;
                        mCurrentDrawState = DRAW_STATE.DRAW_BOARD;
                    }
                }

                mBoard.draw(mMVPMatrix);
                break;

            default:
                mBoard.draw(mMVPMatrix);
                break;
        }
    }

    // Screen was touched at x,y coord
    public void onTouchEvent(MotionEvent event) {
        if (mCurrentDrawState == DRAW_STATE.DRAW_BOARD &&
                (event.getAction() == MotionEvent.ACTION_DOWN)) {
            float normalX = getNormalizedXCoord(event.getX());
            float normalY = getNormalizedYCoord(event.getY());

            int selectedQuadrant = mBoard.getQuadrantXY(normalX, normalY);

            lastToggled = selectedQuadrant;

            if (selectedQuadrant != -1) {
                mBoard.toggleQuadrant(selectedQuadrant);

                //playPattern();
                //addRandomPatternItem();
            }
        } if (event.getAction() == MotionEvent.ACTION_UP) {
            mBoard.toggleQuadrant(lastToggled);
        }
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
        mCurrentDrawState = DRAW_STATE.DRAW_PATTERN;
    }
}
