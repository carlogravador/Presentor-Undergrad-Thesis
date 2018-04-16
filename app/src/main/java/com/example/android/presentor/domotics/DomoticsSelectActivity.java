package com.example.android.presentor.domotics;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.example.android.presentor.R;
import com.example.android.presentor.utils.Utility;

import java.util.ArrayList;
import java.util.Set;

public class DomoticsSelectActivity extends AppCompatActivity {

    public static boolean isActivityOpen;

    @Override
    protected void onResume() {
        super.onResume();
        isActivityOpen = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityOpen = false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_domotics_select);

        final ArrayList<String> btDevice = new ArrayList<>();
        final ArrayList<String> btAddress = new ArrayList<>();

        final ListView pairedDeviceListView = (ListView) findViewById(R.id.list_view_paired_devices);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.loading_indicator);
        pairedDeviceListView.setEmptyView(progressBar);

        final PairedDevicesAdapter pda = new PairedDevicesAdapter(this, 0, btDevice);
        pairedDeviceListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final String name = btDevice.get((int) l);
                final String address = btAddress.get((int) l);
                final Intent intent = new Intent(DomoticsSelectActivity.this, DomoticsActivity.class);
                intent.putExtra(DomoticsActivity.BT_DEVICE, name);
                intent.putExtra(DomoticsActivity.BT_ADDRESS, address);

                if(!Utility.getBoolean(getApplicationContext(), getResources().getString(R.string.pref_auto_connect_key))) {

                    Utility.showDomoticsDialogDialog(DomoticsSelectActivity.this,
                            "Always",
                            "Just Once",
                            null,
                            "Always connect to " + name + " Bluetooth device?",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    switch (i) {
                                        case DialogInterface.BUTTON_NEGATIVE:
                                            Utility.saveBoolean(getApplicationContext(),
                                                    getResources().getString(R.string.pref_auto_connect_key),
                                                    false);
                                            startActivity(intent);
                                            break;
                                        case DialogInterface.BUTTON_POSITIVE:
                                            Utility.saveBoolean(getApplicationContext(),
                                                    getResources().getString(R.string.pref_auto_connect_key),
                                                    true);
                                            Utility.saveString(getApplicationContext(),
                                                    getResources().getString(R.string.pref_bt_device_key),
                                                    address);
                                            Utility.saveString(getApplicationContext(),
                                                    DomoticsActivity.BT_DEVICE,
                                                    name);
                                            startActivity(intent);
                                            finish();
                                            break;
                                    }
                                }
                            });
                }


            }
        });
//

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                BluetoothAdapter bluetoothAdapter;
                final Set<BluetoothDevice> pairedDevices;

                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                pairedDevices = bluetoothAdapter.getBondedDevices();

                for (BluetoothDevice bt : pairedDevices) {
                    btDevice.add(bt.getName());
                    btAddress.add(bt.getAddress());
                }
                pairedDeviceListView.setAdapter(pda);
            }
        },1200);
    }
}
