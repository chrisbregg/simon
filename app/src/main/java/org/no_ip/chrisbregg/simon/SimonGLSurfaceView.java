package org.no_ip.chrisbregg.simon;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

/**
 * Created by Chris on 2015-03-02.
 */
public class SimonGLSurfaceView extends GLSurfaceView {
    private SimonGLRenderer mRenderer;

    public SimonGLSurfaceView(Context context) {
        super(context);

        // create an opengl es 2.0 context
        setEGLContextClientVersion(2);

        mRenderer = new SimonGLRenderer();

        setRenderer(mRenderer);

        // render only when told to
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mRenderer.onTouch(event.getX(), event.getY());

            requestRender();
        }

        return true;
    }
}
