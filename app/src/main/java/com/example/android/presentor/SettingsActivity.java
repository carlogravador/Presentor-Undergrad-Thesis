package com.example.android.presentor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.android.presentor.domotics.DomoticsActivity;
import com.example.android.presentor.utils.Utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity {


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


    }

    public static class MySettingsFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener {

        @Override
        public void onResume() {
            Preference domoticsPref = findPreference(getResources().getString(R.string.pref_auto_connect_key));
            bindPreferenceSummaryToValue(domoticsPref);

            Preference btAddressPref = findPreference(getResources().getString(R.string.pref_bt_device_key));
            setListPreferenceData((ListPreference) btAddressPref);
            bindPreferenceSummaryToValue(btAddressPref);

            super.onResume();
        }

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
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int prefIndex = listPreference.findIndexOfValue(value);
                CharSequence[] labels = listPreference.getEntries();
                if (prefIndex >= 0) {
                    Utility.saveString(getActivity().getApplicationContext(),
                            DomoticsActivity.BT_DEVICE,
                            (String) labels[prefIndex]
                    );
                    preference.setSummary(labels[prefIndex]);
                } else {
                    prefIndex = listPreference.findIndexOfValue("00:21:13:00:99:DA");
                    if (prefIndex >= 0) {
                        Utility.saveString(getActivity().getApplicationContext(),
                                DomoticsActivity.BT_DEVICE,
                                (String) labels[prefIndex]
                        );
                        preference.setSummary(labels[prefIndex]);
                    } else {
                        Utility.saveString(getActivity().getApplicationContext(),
                                DomoticsActivity.BT_DEVICE,
                                (String) labels[0]
                        );
                        preference.setSummary(labels[0]);
                    }
                }
            } else if(preference instanceof CheckBoxPreference){
                boolean b = (boolean)o;
                CheckBoxPreference cbp = (CheckBoxPreference) preference;
                cbp.setChecked(b);
            }else {
                preference.setSummary(value);
            }
            return true;
        }

        private void setListPreferenceData(ListPreference lp) {
            BluetoothAdapter bluetoothAdapter;
            Set<BluetoothDevice> pairedDevices;

            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            pairedDevices = bluetoothAdapter.getBondedDevices();

            List<String> btDevicesList = new ArrayList<>();
            for (BluetoothDevice bt : pairedDevices) {
                btDevicesList.add(bt.getName());
            }

            String[] btDevicesArray = btDevicesList.toArray(new String[0]);

            List<String> btAddressList = new ArrayList<>();
            for (BluetoothDevice bt : pairedDevices) {
                btAddressList.add(bt.getAddress());
            }
            String[] btAddressArray = btAddressList.toArray(new String[0]);

            lp.setEntries(btDevicesArray);
            lp.setEntryValues(btAddressArray);
            int prefIndex = lp.findIndexOfValue("00:21:13:00:99:DA");
            if (prefIndex >= 0) {
                lp.setValueIndex(prefIndex);
            } else {
                lp.setValueIndex(0);
            }
        }


        private void bindPreferenceSummaryToValue(Preference preference) {
            preference.setOnPreferenceChangeListener(this);
            SharedPreferences preferences =
                    PreferenceManager.getDefaultSharedPreferences(preference.getContext());
            if(preference instanceof CheckBoxPreference){
                boolean preferenceBoolean = preferences.getBoolean(preference.getKey(), false);
                onPreferenceChange(preference, preferenceBoolean);
                return;
            }
            String preferenceString = preferences.getString(preference.getKey(), "");
            onPreferenceChange(preference, preferenceString);
        }
    }
}
