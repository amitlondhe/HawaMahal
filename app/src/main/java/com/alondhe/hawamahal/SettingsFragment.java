package com.alondhe.hawamahal;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;

/**
 * Created by alondhe on 1/8/2016.
 */
public class SettingsFragment extends PreferenceFragment  implements SharedPreferences.OnSharedPreferenceChangeListener  {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//        Log.d("New Value","************");
        String value = sharedPreferences.getString(key,MainActivity.TEMP_MARGIN);
        MainActivity.TEMP_MARGIN = value;
//        Log.d("New Value", MainActivity.TEMP_MARGIN);
    }
}
