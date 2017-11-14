package com.example.android.presentor.Domotics;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.example.android.presentor.R;

import java.util.ArrayList;

public class DomoticsActivity extends AppCompatActivity {

    private final static String LOG_TAG = DomoticsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_domotics);

        ListView switchListView = (ListView)findViewById(R.id.list_view_connected_devices);

        final ArrayList<DomoticsSwitch> domoticsSwitches = new ArrayList<>();


        for(int i = 1; i <= 8; i++){
            String switchText = getResources().getString(R.string.appliance_name) + " " + i;
            domoticsSwitches.add(new DomoticsSwitch(false, switchText));
            Log.e(LOG_TAG, switchText);
        }

        DomoticsSwitchAdapter domoticsSwitchAdapter = new DomoticsSwitchAdapter(this, 0, domoticsSwitches);
        switchListView.setAdapter(domoticsSwitchAdapter);
    }

}
