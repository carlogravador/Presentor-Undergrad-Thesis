package com.example.android.presentor.utils;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;


import com.example.android.presentor.R;
import com.example.android.presentor.domotics.DomoticsActivity;


/**
 * Created by Carlo on 19/11/2017.
 */

public class Utility {

    public static void showAlertDialog(Context context, String title, String message,
                                       DialogInterface.OnClickListener listener){

        AlertDialog.Builder adb = new AlertDialog.Builder(context);
        adb.setTitle(title);
        adb.setMessage(message);
        adb.setPositiveButton(R.string.yes, listener);
        adb.setNegativeButton(R.string.no, listener);
        AlertDialog alertDialog = adb.create();
        alertDialog.show();
    }

    public static void renameAppliance(Context context, String applianceName){
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        };

        AlertDialog.Builder adb = new AlertDialog.Builder(context);
        adb.setTitle("Rename Appliance");
        EditText et = new EditText(context);
        FrameLayout container = new FrameLayout(context);
        FrameLayout.LayoutParams params= new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        int margins = context.getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.setMargins(margins, margins, margins, 0);
        et.setLayoutParams(params);
        et.setText(applianceName);
        et.setSelectAllOnFocus(true);
        et.setSingleLine();
        container.addView(et);
        adb.setView(container);
        adb.setPositiveButton(R.string.rename, listener);
        adb.setNegativeButton(R.string.cancel, listener);

        AlertDialog alertDialog = adb.create();
        alertDialog.show();
    }

    public static boolean isBluetoothOn(){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null){
            //Device does not support Bluetooth
            //TODO show dialog that tells device does not support bluetooth
            return false;
        }else{
            if(bluetoothAdapter.isEnabled()){
                return true;
            }
            else{
                return  false;
            }
        }
    }

    public static void turnOnBluetooth(final Context context){
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch(i){
                    case DialogInterface.BUTTON_POSITIVE:
                        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        bluetoothAdapter.enable();
                        context.startActivity(new Intent(context, DomoticsActivity.class));
                }
            }
        };
        AlertDialog.Builder adb = new AlertDialog.Builder(context);
        adb.setTitle("Bluetooth is off");
        adb.setMessage("Domotics requires bluetooth connection. \n\n" +
                "Do you want to open bluetooth now?");
        adb.setPositiveButton(R.string.yes, listener);
        adb.setNegativeButton(R.string.no, listener);
        AlertDialog alertDialog = adb.create();
        alertDialog.show();
    }




    public static boolean isWifiConnected(Context context) {
        WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiMgr.isWifiEnabled()) { // WiFi adapter is ON
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            if (wifiInfo != null && wifiInfo.getNetworkId() == -1) {
                return false; // Not connected to an access-Point
            }
            return true;      // Connected to an Access Point
        } else {
            return false; // WiFi adapter is OFF
        }
    }


    public static void clearKey(Context cxt, String key) {
        SharedPreferences.Editor prefsEditor = cxt.getSharedPreferences("kkd", Context.MODE_PRIVATE).edit();
        prefsEditor.remove(key);
        prefsEditor.commit();
    }

    public static void saveString(Context cxt, String key, String value) {
        SharedPreferences.Editor prefsEditor = cxt.getSharedPreferences("kkd", Context.MODE_PRIVATE).edit();
        prefsEditor.putString(key, value);
        prefsEditor.commit();
    }

    public static String getString(Context cxt, String key) {
        SharedPreferences prefs = cxt.getSharedPreferences("kkd", Context.MODE_PRIVATE);
        String val = prefs.getString(key, null);
        return val;
    }

    public static void saveInt(Context cxt, String key, int value) {
        SharedPreferences.Editor prefsEditor = cxt.getSharedPreferences("kkd", Context.MODE_PRIVATE).edit();
        prefsEditor.putInt(key, value);
        prefsEditor.commit();
    }

    public static int getInt(Context cxt, String key) {
        SharedPreferences prefs = cxt.getSharedPreferences("kkd", Context.MODE_PRIVATE);
        int val = prefs.getInt(key, -1);
        return val;
    }




}
