package com.alondhe.hawamahal;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by alondhe on 1/8/2016.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}
