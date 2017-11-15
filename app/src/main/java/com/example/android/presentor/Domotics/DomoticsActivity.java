package com.example.android.presentor.Domotics;

import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_domotics);

        initMasterSwitch();

        String[] applianceName = this.getResources().getStringArray(R.array.appliance_name);

        ListView listViewDomotics = (ListView) findViewById(R.id.list_view_domotics);

        ArrayList<DomoticsSwitch> domoticsSwitches = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            String appliance = applianceName[i];
            domoticsSwitches.add(new DomoticsSwitch(appliance, false));
        }

        DomoticsSwitchAdapter dsAdapter = new DomoticsSwitchAdapter(this, 0, domoticsSwitches);
        listViewDomotics.setAdapter(dsAdapter);

    }

    private void initMasterSwitch(){
        CardView masterSwitchCardView = (CardView)findViewById(R.id.card_view_master_switch);
        final TextView masterSwitchStatusText = (TextView)findViewById(R.id.text_view_master_switch_status);
        final ImageView masterSwitchStatus = (ImageView)findViewById(R.id.image_view_master_switch_status);
        final Switch masterSwitch = (Switch)findViewById(R.id.switch_master);

        masterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    Drawable d = DomoticsActivity.this.getResources().
                            getDrawable(R.drawable.ic_power_bg_on);
                    masterSwitchStatusText.setText(DomoticsActivity.this.getResources().
                            getString(R.string.appliance_status_on));
                    masterSwitchStatus.setBackground(d);
                }else{
                    Drawable d = DomoticsActivity.this.getResources().
                            getDrawable(R.drawable.ic_power_bg_off);
                    masterSwitchStatusText.setText(DomoticsActivity.this.getResources().
                            getString(R.string.appliance_status_off));
                    masterSwitchStatus.setBackground(d);
                }
            }
        });

        masterSwitchCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                masterSwitch.setChecked(!masterSwitch.isChecked());
            }
        });
    }
}
