package com.example.android.presentor.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.example.android.presentor.R;
import com.example.android.presentor.domotics.DomoticsActivity;
import com.example.android.presentor.domotics.DomoticsSwitch;

import org.w3c.dom.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by Carlo on 19/11/2017.
 */

public class Utility {

    public static final int REQUEST_CAMERA_PERM = 69;

    public static void showAlertDialog(Context context, String title, String message,
                                       DialogInterface.OnClickListener listener) {

        AlertDialog.Builder adb = new AlertDialog.Builder(context);
        adb.setTitle(title);
        adb.setMessage(message);
        adb.setPositiveButton(R.string.yes, listener);
        adb.setNegativeButton(R.string.no, listener);
        AlertDialog alertDialog = adb.create();
        alertDialog.show();
    }

    //temp only
    public static void showConnectDialog(final Context context, final Intent intent, String lobbyName,
                                         String creatorName, String ip, final String password) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View passwordAlertLayout = inflater.inflate(R.layout.create_lobby_custom_dialog, null);

        View passwordContainer = passwordAlertLayout.findViewById(R.id.dialog_password_panel);

        TextView titleTv = passwordAlertLayout.findViewById(R.id.dialog_title);
        TextView creatorTv = passwordAlertLayout.findViewById(R.id.dialog_creator);
        TextView ipTv = passwordAlertLayout.findViewById(R.id.dialog_ip);
        final EditText passwordEt = passwordAlertLayout.findViewById(R.id.dialog_password_et);
        CheckBox passwordCb = passwordAlertLayout.findViewById(R.id.dialog_password_cb);

        Button cancelBtn = passwordAlertLayout.findViewById(R.id.dialog_cancel_btn);
        Button connectBtn = passwordAlertLayout.findViewById(R.id.dialog_connect_btn);

        titleTv.setText(lobbyName);
        creatorTv.setText(creatorName);
        ipTv.setText(ip);


        if (password.length() == 0) {
            passwordContainer.setVisibility(View.GONE);
        }

        final Dialog customDialog = new Dialog(context);
        customDialog.setContentView(passwordAlertLayout);
        customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customDialog.show();

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialog.dismiss();
            }
        });

        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //has password
                if (password.length() != 0) {
                    //input password is wrong
                    if (!passwordEt.getText().toString().equals(password)) {
                        passwordEt.setError("Password is incorrect.");
                        return;
                    }
                }
                context.startActivity(intent);
                customDialog.dismiss();
            }
        });

        passwordCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    passwordEt.setTransformationMethod(HideReturnsTransformationMethod
                            .getInstance());
                    passwordEt.setSelection(passwordEt.length());
                } else {
                    passwordEt.setTransformationMethod(PasswordTransformationMethod
                            .getInstance());
                    passwordEt.setSelection(passwordEt.length());
                }
            }
        });
    }


    public static boolean isCameraPermissionGranted(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request the camera permission
     */
    public static void requestCameraPermission(Activity activity) {
        final String[] permissions = new String[]{Manifest.permission.CAMERA};
        ActivityCompat.requestPermissions(activity, permissions, REQUEST_CAMERA_PERM);
    }


    public static boolean isBluetoothOn() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    public static void turnOnBluetooth(final Context context, final Intent intent) {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case DialogInterface.BUTTON_POSITIVE:
                        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        bluetoothAdapter.enable();
                        context.startActivity(intent);
                }
            }
        };
        //TODO add hard coded strings to string.xml
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
        WifiManager wifiMgr = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiMgr.isWifiEnabled()) { // WiFi adapter is ON
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            return wifiInfo != null && wifiInfo.getNetworkId() != -1;
        } else {
            return false; // WiFi adapter is OFF
        }
    }


    public static void clearKey(Context cxt, String key) {
        SharedPreferences.Editor prefsEditor = cxt.getSharedPreferences("presentor",
                Context.MODE_PRIVATE).edit();
        prefsEditor.remove(key);
        prefsEditor.commit();
    }

    public static void saveString(Context cxt, String key, String value) {
        SharedPreferences.Editor prefsEditor = cxt.getSharedPreferences("presentor",
                Context.MODE_PRIVATE).edit();
        prefsEditor.putString(key, value);
        prefsEditor.commit();
    }


    public static String getString(Context cxt, String key) {
        SharedPreferences prefs = cxt.getSharedPreferences("presentor", Context.MODE_PRIVATE);
        return prefs.getString(key, null);
    }

    public static void saveInt(Context cxt, String key, int value) {
        SharedPreferences.Editor prefsEditor = cxt.getSharedPreferences("presentor",
                Context.MODE_PRIVATE).edit();
        prefsEditor.putInt(key, value);
        prefsEditor.commit();
    }

    public static int getInt(Context cxt, String key) {
        SharedPreferences prefs = cxt.getSharedPreferences("presentor", Context.MODE_PRIVATE);
        return prefs.getInt(key, -1);
    }

    public static void saveBoolean(Context cxt, String key, boolean value) {
        SharedPreferences.Editor prefsEditor = cxt.getSharedPreferences("presentor",
                Context.MODE_PRIVATE).edit();
        prefsEditor.putBoolean(key, value);
        prefsEditor.commit();
    }

    /***returns true if there is a special character on the String word***/
    public static boolean checkForSpecialCharacter(String word) {
        Pattern p = Pattern.compile("[^A-Za-z0-9]");
        Matcher m = p.matcher(word);

        return m.find();
    }

    public static void showToast(final Context context, final String msg) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
