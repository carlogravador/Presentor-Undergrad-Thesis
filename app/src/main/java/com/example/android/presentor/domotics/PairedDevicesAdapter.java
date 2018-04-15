package com.example.android.presentor.domotics;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.android.presentor.R;

import java.util.List;

public class PairedDevicesAdapter extends ArrayAdapter<String> {


    public PairedDevicesAdapter(@NonNull Context context, int resource, @NonNull List<String> btDevice) {
        super(context, resource, btDevice);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;

        if(listItemView == null){
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_paired_devices, parent, false);
        }

        String currentBtDevice = getItem(position);
        TextView btNameTextView = listItemView.findViewById(R.id.tv_bt_name);

        btNameTextView.setText(currentBtDevice);

        return listItemView;
    }
}
