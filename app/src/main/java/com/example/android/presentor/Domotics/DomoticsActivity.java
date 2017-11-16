package com.example.android.presentor.Domotics;

import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.example.android.presentor.R;

import java.util.ArrayList;

public class DomoticsActivity extends AppCompatActivity {

    private final static String LOG_TAG = DomoticsActivity.class.getSimpleName();

    ArrayList<DomoticsSwitch> mDomoticsSwitches;
    DomoticsSwitchAdapter mDsAdapter;

    public static Switch sMasterSwitch;
    public static boolean sIsAllSwitchOpen = false;
    public static boolean[] sArduinoSwitchesState = new boolean[8];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_domotics);

        initMasterSwitch();

        String[] applianceName = this.getResources().getStringArray(R.array.appliance_name);

        ListView listViewDomotics = (ListView) findViewById(R.id.list_view_domotics);

        mDomoticsSwitches = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            String appliance = applianceName[i];
            sArduinoSwitchesState[i] = false;
            mDomoticsSwitches.add(new DomoticsSwitch(appliance, sArduinoSwitchesState[i]));
        }

        mDsAdapter = new DomoticsSwitchAdapter(this, 0, mDomoticsSwitches);
        listViewDomotics.setAdapter(mDsAdapter);

    }

    private void initMasterSwitch() {
        CardView masterSwitchCardView = (CardView) findViewById(R.id.card_view_master_switch);
        sMasterSwitch = (Switch) findViewById(R.id.switch_master);

        sMasterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                turnMasterSwitch(isChecked);
            }
        });

        masterSwitchCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sMasterSwitch.setChecked(!sMasterSwitch.isChecked());
            }
        });
    }


    private void turnMasterSwitch(boolean state) {

        TextView masterSwitchStatusText = (TextView) findViewById(R.id.text_view_master_switch_status);
        ImageView masterSwitchStatus = (ImageView) findViewById(R.id.image_view_master_switch_status);
        Drawable d;

        if (state) {
            //turn on
            sIsAllSwitchOpen = true;
            turnAllSwitch(true);
            d = DomoticsActivity.this.getResources().getDrawable(R.drawable.ic_power_bg_on);
            masterSwitchStatusText.setText(DomoticsActivity.this.getResources().
                    getString(R.string.appliance_status_on));
        } else {
            //turn off
            if (sIsAllSwitchOpen) {
                sIsAllSwitchOpen = false;
                turnAllSwitch(false);
            }
            d = DomoticsActivity.this.getResources().getDrawable(R.drawable.ic_power_bg_off);
            masterSwitchStatusText.setText(DomoticsActivity.this.getResources().
                    getString(R.string.appliance_status_on));
        }
        masterSwitchStatus.setBackground(d);
    }

    private void turnAllSwitch(boolean b) {
        for (int i = 0; i < 8; i++) {
            mDomoticsSwitches.get(i).setSwitchStatus(b);
            turnOnArduinoSwitch(i, b);
        }
        mDsAdapter.notifyDataSetChanged();
    }

    //this function avoids redundant sending of data to arduino
    public static void turnOnArduinoSwitch(int i, boolean turnOn){
        int powerOn = i+1;
        int powerOff = (i+1) * 11;
        if(turnOn){
            //turn on
            if(!sArduinoSwitchesState[i]){
                Log.d("Arduino", "Turn on " + powerOn );
                sArduinoSwitchesState[i] = true;
            }

        }else{
            //turn off
            if(sArduinoSwitchesState[i]){
                Log.d("Arduino", "Turn off " + powerOff);
                sArduinoSwitchesState[i] = false;
            }

        }
    }




}
