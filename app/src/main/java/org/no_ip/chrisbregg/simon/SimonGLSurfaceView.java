package org.no_ip.chrisbregg.simon;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import java.util.ArrayList;

/**
 * Created by Chris on 2015-03-02.
 */
public class SimonGLSurfaceView extends GLSurfaceView {
    public enum UserAction { ACTION_START };

    private SimonGLRenderer mRenderer;

    public SimonGLSurfaceView(Context context, int gameMode) {
        super(context);

        // create an opengl es 2.0 context
        setEGLContextClientVersion(2);

        mRenderer = new SimonGLRenderer(gameMode);

        setRenderer(mRenderer);

        // render only when told to
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        SoundPlayer.mContext = getContext();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mRenderer.onTouchEvent(event);

        return true;
    }

    public void actionBar(UserAction action) {
        if (action == UserAction.ACTION_START) {
            mRenderer.initPattern();
        }
    }
}
