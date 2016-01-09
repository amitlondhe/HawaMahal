package com.alondhe.hawamahal;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by alondhe on 1/8/2016.
 */
public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
