package org.no_ip.chrisbregg.simon;

import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * Created by Chris on 2015-03-02.
 */
public class SimonGLSurfaceView extends GLSurfaceView {
    private SimonGLRenderer mRenderer;

    public SimonGLSurfaceView(Context context) {
        super(context);

        mRenderer = new SimonGLRenderer();

        setRenderer(mRenderer);

        // render only when told to
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}
