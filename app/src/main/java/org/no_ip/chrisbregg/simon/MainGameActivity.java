package org.no_ip.chrisbregg.simon;

import android.app.Activity;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;


public class MainGameActivity extends ActionBarActivity {
    public static final int GAME_MODE_CLASSIC = 1;
    public static final int GAME_MODE_REVERSE = 2;

    public static final String GAME_MODE_TAG = "GameMode";

    private SimonGLSurfaceView mGLView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();

        int mode = 0;

        if (extras != null) {
            mode = extras.getInt(GAME_MODE_TAG);
        }

        mGLView = new SimonGLSurfaceView(this, mode);

        setContentView(mGLView);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_game, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_start) {
            mGLView.actionBar(SimonGLSurfaceView.UserAction.ACTION_START);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
