package com.example.android.presentor.domotics;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Carlo on 20/11/2017.
 */

public class BtConnectAsyncTask extends AsyncTask<Void, Void, Void> {

    private Context mContext;

    private boolean mOnConnectSuccess = false;
    public static boolean mIsConnected = false;
    private ProgressDialog mProgressDialog;

    private BluetoothAdapter mBluetoothAdapter;
    private static BluetoothSocket mBluetoothSocket;

    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    String address = "00:21:13:00:99:DA";      //temporary

    public BtConnectAsyncTask(Context context) {
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
       mProgressDialog = ProgressDialog.show(mContext, null, "Connecting to Presentor...");
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
            mContext.startActivity(new Intent(mContext, DomoticsActivity.class));
        }
        else{
            //connection failed
            mProgressDialog.cancel();
            this.cancel(true);
        }
    }
}
