package org.no_ip.chrisbregg.simon;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by Chris on 2015-03-05.
 */
public class GameBoard {
    private GameBoardQuadrant mQuadrants[];

    public GameBoard() {
        mQuadrants = new GameBoardQuadrant[4];

        mQuadrants[0] = new GameBoardQuadrant(0.0f, (float)0.5 * (float)Math.PI);
        mQuadrants[0].setColour(1.0f, 0.0f, 0.0f, 1.0f);

        mQuadrants[1] = new GameBoardQuadrant((float)0.5 * (float)Math.PI, (float)Math.PI);
        mQuadrants[1].setColour(0.0f, 0.0f, 1.0f, 1.0f);

        mQuadrants[2] = new GameBoardQuadrant((float)Math.PI, (float)1.5 * (float)Math.PI);
        mQuadrants[2].setColour(0.0f, 1.0f, 0.0f, 1.0f);

        mQuadrants[3] = new GameBoardQuadrant((float)1.5 * (float)Math.PI, (float)2.0 * (float)Math.PI);
        mQuadrants[3].setColour(1.0f, 1.0f, 0.0f, 1.0f);
    }

    public void draw(float[] mvpMatrix) {
        for (int x = 0; x < mQuadrants.length; x++) {
            mQuadrants[x].draw(mvpMatrix);
        }
    }
}
