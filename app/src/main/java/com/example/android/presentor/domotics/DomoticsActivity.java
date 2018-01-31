package com.example.android.presentor.domotics;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.example.android.presentor.R;
import com.example.android.presentor.utils.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class DomoticsActivity extends AppCompatActivity {

    private final static String LOG_TAG = DomoticsActivity.class.getSimpleName();

    ArrayList<DomoticsSwitch> mDomoticsSwitches;
    DomoticsSwitchAdapter mDsAdapter;



    private static boolean mIsConnected = false;
    private ProgressDialog mProgressDialog;

    private BluetoothAdapter mBluetoothAdapter;
    private static BluetoothSocket mBluetoothSocket;

    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    String address = "00:21:13:00:99:DA";      //temporary

    public static Switch sMasterSwitch;
    public static boolean sIsAllSwitchOpen = false;
    public static boolean[] sArduinoSwitchesState = new boolean[8];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_domotics);

        new BtConnectAsyncTask().execute();
        initMasterSwitch();

        String[] applianceNameKey = this.getResources().getStringArray(R.array.appliance_name);

        ListView listViewDomotics = (ListView) findViewById(R.id.list_view_domotics);

        mDomoticsSwitches = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            String applianceName = initDomoticsName(applianceNameKey, i);

            sArduinoSwitchesState[i] = false;
            mDomoticsSwitches.add(new DomoticsSwitch(applianceName, sArduinoSwitchesState[i]));
        }

        mDsAdapter = new DomoticsSwitchAdapter(this, 0, mDomoticsSwitches);
        listViewDomotics.setAdapter(mDsAdapter);

    }

    private String initDomoticsName(String[] applianceNameKey, int keyIndex){
        String applianceKey = applianceNameKey[keyIndex];
        String applianceName = Utility.getString(this, applianceKey);
        if (applianceName  == null){
            applianceName = applianceKey;
            Utility.saveString(this, applianceKey, applianceName);
        }
        return applianceName;
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
                turnOnLed(i+1);
            }

        }else{
            //turn off
            if(sArduinoSwitchesState[i]){
                Log.d("Arduino", "Turn off " + powerOff);
                sArduinoSwitchesState[i] = false;
                turnOffLed(i+1);
            }

        }
    }


    class BtConnectAsyncTask extends AsyncTask<Void, Void, Void> {

        private boolean mOnConnectSuccess = true;

        @Override
        protected void onPreExecute() {
            mProgressDialog = ProgressDialog.show(DomoticsActivity.this, null, "Connecting to Presentor");
        }

        @Override
        protected Void doInBackground(Void... devices) {
            try
            {
                if (mBluetoothSocket == null || !mOnConnectSuccess)
                {
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice presentorBtDevice = mBluetoothAdapter.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    mBluetoothSocket = presentorBtDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    mBluetoothSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                mOnConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(mOnConnectSuccess){
                mIsConnected = true;
            }
            else{
                //connection failed
                finish();
            }
            mProgressDialog.dismiss();
        }
    }


    public static void turnOffLed(int switchNumber)
    {
        switchNumber = switchNumber * 11;
        if (mBluetoothSocket!=null)
        {
            try
            {
                mBluetoothSocket.getOutputStream().write(switchNumber);
            }
            catch (IOException e)
            {
                //msg("Error");
            }
        }
    }

    public static void turnOnLed(int switchNumber)
    {
        if (mBluetoothSocket!=null)
        {
            try
            {
                mBluetoothSocket.getOutputStream().write(switchNumber);
            }
            catch (IOException e)
            {
                //msg("Error");
            }
        }
    }


}
