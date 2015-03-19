package org.no_ip.chrisbregg.simon;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

/**
 * Created by Chris on 2015-03-05.
 */
public class GameBoard {
    private GameBoardQuadrant mQuadrants[];

    private SoundPlayer mSoundPlayer;

    public GameBoard() {
        mQuadrants = new GameBoardQuadrant[4];

        mQuadrants[0] = new GameBoardQuadrant(0.0f, (float)0.5 * (float)Math.PI);
        mQuadrants[0].setOnColour(1.0f, 0.0f, 0.0f, 1.0f);
        mQuadrants[0].setOffColour(0.25f, 0.0f, 0.0f, 1.0f);

        mQuadrants[1] = new GameBoardQuadrant((float)0.5 * (float)Math.PI, (float)Math.PI);
        mQuadrants[1].setOnColour(0.0f, 0.0f, 1.0f, 1.0f);
        mQuadrants[1].setOffColour(0.0f, 0.0f, 0.25f, 1.0f);

        mQuadrants[2] = new GameBoardQuadrant((float)Math.PI, (float)1.5 * (float)Math.PI);
        mQuadrants[2].setOnColour(0.0f, 1.0f, 0.0f, 1.0f);
        mQuadrants[2].setOffColour(0.0f, 0.25f, 0.0f, 1.0f);

        mQuadrants[3] = new GameBoardQuadrant((float)1.5 * (float)Math.PI, (float)2.0 * (float)Math.PI);
        mQuadrants[3].setOnColour(1.0f, 1.0f, 0.0f, 1.0f);
        mQuadrants[3].setOffColour(0.25f, 0.25f, 0.0f, 1.0f);

        mSoundPlayer = new SoundPlayer();
    }

    public void draw(float[] mvpMatrix) {
        for (int x = 0; x < mQuadrants.length; x++) {
            mQuadrants[x].draw(mvpMatrix);
        }
    }

    public void toggleQuadrant(int x) {
        if (x < mQuadrants.length) {
            mQuadrants[x].toggleLight();

            if (mQuadrants[x].isOn()) {
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
    }

    public int getQuadrantCount() {
        return mQuadrants.length;
    }


}
