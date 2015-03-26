package org.no_ip.chrisbregg.simon;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

/**
 * Created by Chris on 2015-03-05.
 */
public class GameBoard {
    private GameBoardQuadrant mQuadrants[];



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
    }

    public void draw(float[] mvpMatrix) {
        for (int x = 0; x < mQuadrants.length; x++) {
            mQuadrants[x].draw(mvpMatrix);
        }
    }

    public boolean toggleQuadrant(int x) {
        if (x < mQuadrants.length && x >= 0) {
            return mQuadrants[x].toggleLight();
        } else {
            return false;
        }
    }

    // Get the quadrant that contains the given coord pair
    // returns -1 if not inside the board
    public int getQuadrantXY(float x, float y) {
        for (int count = 0; count < mQuadrants.length; count++) {
            if (mQuadrants[count].isInsideQuadrant(x, y)) {
                return count;
            }
        }

        return -1;
    }

    public int getQuadrantCount() {
        return mQuadrants.length;
    }


}
