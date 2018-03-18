package com.example.android.presentor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Set;

public class SettingsActivity extends AppCompatActivity {


//    private BluetoothAdapter mBluetoothAdapter;
//    private Set<BluetoothDevice> pairedDevices;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

//        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        pairedDevices = mBluetoothAdapter.getBondedDevices();
    }

    public static class MySettingsFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener {


        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_main);

            Preference playerNamePref = findPreference(getResources().getString(R.string.pref_player_key));
            bindPreferenceSummaryToValue(playerNamePref);

            Preference resizeImagePref = findPreference(getResources().getString(R.string.pref_mirroring_resize_key));
            bindPreferenceSummaryToValue(resizeImagePref);

            Preference qualityImagePref = findPreference(getResources().getString(R.string.pref_mirroring_quality_key));
            bindPreferenceSummaryToValue(qualityImagePref);
        }

        //------------implment onPreferenceChangeListener------------------//
        @Override
        public boolean onPreferenceChange(Preference preference, Object o) {
            String value = o.toString();
            preference.setSummary(value);
            return true;
        }

        private void bindPreferenceSummaryToValue(Preference preference) {
            preference.setOnPreferenceChangeListener(this);
            SharedPreferences preferences =
                    PreferenceManager.getDefaultSharedPreferences(preference.getContext());
            String preferenceString = preferences.getString(preference.getKey(), "");
            onPreferenceChange(preference, preferenceString);
        }
    }
}
