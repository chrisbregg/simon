package org.no_ip.chrisbregg.simon;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by Chris on 2015-03-05.
 */
public class GameBoardQuadrant {
    // the number of triangles used to make up the circle, higher is cleaner but slower
    private final int CIRCLE_PRECISION = 8;

    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}";

    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;

    // access and set the view transformation
    private int mMVPMatrixHandle;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    private float circleCoords[];

    private float radius = 0.95f;

    private short drawOrder[];

    private float onColour[];
    private float offColour[];

    private boolean isOn;

    private final int mProgram;

    private int mPositionHandle;
    private int mColorHandle;

    private final int vertexCount;
    private final int vertexStride = COORDS_PER_VERTEX * 4;

    private float mMinRads = (float)Math.PI / 2.0f; // Where the circle segment should start
    private float mMaxRads = (float)Math.PI;// where the circle segment should end

    public GameBoardQuadrant(float minRads, float maxRads) {
        setMinRads(minRads);
        setMaxRads(maxRads);

        isOn = false;

        onColour = new float[4];
        offColour = new float[4];
        setOnColour(0, 0, 0, 1); // default to black
        setOffColour(0, 0, 0, 1); // default to black

        generateCircleCoords();
        generateDrawOrder();

        vertexCount =  circleCoords.length / COORDS_PER_VERTEX;

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate vlues * 4 bytes per short)
                circleCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(circleCoords);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // # of coordinate values * 2 bytes per short
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        int vertexShader = SimonGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = SimonGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        // create empty OpenGL ES program
        mProgram = GLES20.glCreateProgram();

        // add the vertex shader to program
        GLES20.glAttachShader(mProgram, vertexShader);

        // add the fragment shader
        GLES20.glAttachShader(mProgram, fragmentShader);

        // create opengl es program executable
        GLES20.glLinkProgram(mProgram);
    }

    private void generateCircleCoords() {
        // create 1 extra point for the origin
        circleCoords = new float[(CIRCLE_PRECISION + 2) * COORDS_PER_VERTEX];

        circleCoords[0] = 0.0f; // origin x;
        circleCoords[1] = 0.0f; // origin y;
        circleCoords[2] = 0.0f; // origin z;

        float radRange = mMaxRads - mMinRads;
        float radsPerTriangle = (radRange / (float)CIRCLE_PRECISION);

        for (int x = 0; x < CIRCLE_PRECISION; x++) {
            // Shift up by 1 coordinate position since the 0 position is the origin
            circleCoords[(x + 1) * COORDS_PER_VERTEX] = radius * (float)Math.cos(mMinRads + (radsPerTriangle * (float)x)); // point x-coord
            circleCoords[((x + 1) * COORDS_PER_VERTEX) + 1] = radius * (float)Math.sin(mMinRads + (radsPerTriangle * (float) x)); // point y-coord
            circleCoords[((x + 1) * COORDS_PER_VERTEX) + 2] = 0.0f; // point z-coord
        }

        // the last set of coords should match the maxrads exactly
        // Shifted up by 1 since 0,1,2 is the origin
        circleCoords[(CIRCLE_PRECISION + 1) * COORDS_PER_VERTEX] = radius * (float)Math.cos(mMaxRads); // point x-coord
        circleCoords[((CIRCLE_PRECISION + 1) * COORDS_PER_VERTEX) + 1] = radius * (float)Math.sin(mMaxRads); // point y-coord
        circleCoords[((CIRCLE_PRECISION + 1) * COORDS_PER_VERTEX) + 2] = 0.0f; // point z-coord
    }

    private void generateDrawOrder() {
        int drawnVertices = CIRCLE_PRECISION * 3; // number of triangles x number of points per triangle
        drawOrder = new short[drawnVertices];

        short currentVertex = 1;

        for (int x = 0; x < drawnVertices; x++) {
            switch (x % 3) {
                case 0:
                    drawOrder[x] = 0; // this is the origin of the triangle
                    break;
                case 1:
                    drawOrder[x] = currentVertex++;
                    break;
                case 2:
                    drawOrder[x] = currentVertex; // we will need to reuse this vertex, so stay on it
                    break;
            }
        }
    }

    public void draw(float[] mvpMatrix) {
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // set color for drawing the triangle
        if (isOn) {
            GLES20.glUniform4fv(mColorHandle, 1, onColour, 0);
        } else {
            GLES20.glUniform4fv(mColorHandle, 1, offColour, 0);
        }

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vertexCount);

        // disable the vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    public void toggleLight() {
        isOn = !isOn;
    }

    public boolean isOn() {
        return isOn;
    }

    public void setMinRads(float minRads) {
        if (minRads >= 0) {
            this.mMinRads = minRads;
        } else {
            this.mMinRads = 0;
        }
    }

    public void setMaxRads(float maxRads) {
        if (maxRads >= 0) {
            this.mMaxRads = maxRads;
        } else {
            this.mMaxRads = 0;
        }
    }

    public void setOnColour(float r, float g, float b, float a) {
        if (r >= 0 && g >= 0 && b >= 0 && a >= 0) {
            onColour[0] = r;
            onColour[1] = g;
            onColour[2] = b;
            onColour[3] = a;
        }
    }

    public void setOffColour(float r, float g, float b, float a) {
        if (r >= 0 && g >= 0 && b >= 0 && a >= 0) {
            offColour[0] = r;
            offColour[1] = g;
            offColour[2] = b;
            offColour[3] = a;
        }
    }

    // Determines if the given coords are inside this quadrant
    public boolean isInsideQuadrant(float x, float y) {
        double distanceFromCenter = Math.sqrt((x * x) + (y * y));

        // If this coord pair is outside the max radius, it isn't inside the quadrant
        if (distanceFromCenter > radius) {
            return false;
        }

        // Find the radians to the given coord pair and determine if it is between the max and min radians
        double radians = Math.atan(y / x);

        // If in quadrant 2 or 3
        if (x < 0) {
            radians += Math.PI;
        } else if (x > 0 && y < 0) {
            radians += 2 * Math.PI;
        }

        if (mMinRads < radians && radians < mMaxRads) {
            return true;
        } else {
            return false;
        }
    }
}
