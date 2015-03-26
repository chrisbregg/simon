package org.no_ip.chrisbregg.simon;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

/**
 * Created by Chris on 2015-03-25.
 */
public class MainActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_activity);
    }

    public void button_start_onClick(View view) {
        Intent intent = new Intent(this, MainGameActivity.class);

        startActivity(intent);
    }
}
