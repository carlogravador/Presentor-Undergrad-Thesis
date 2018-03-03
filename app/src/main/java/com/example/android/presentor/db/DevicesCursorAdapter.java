package com.example.android.presentor.db;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.presentor.R;
import com.example.android.presentor.db.ServicesContract.DeviceEntry;

/**
 * Created by Carlo on 12/02/2018.
 */

public class DevicesCursorAdapter extends CursorAdapter {

    Context mContext;

    public DevicesCursorAdapter(Context context, Cursor c){
        super(context, c, 0);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_devices, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView tvName = (TextView) view.findViewById(R.id.tv_connected_name);
        TextView tvAddress = (TextView) view.findViewById(R.id.tv_connected_ip);

        int deviceNameIndex = cursor.getColumnIndex(DeviceEntry.COL_DEV_NAME);
        int deviceIpIndex = cursor.getColumnIndex(DeviceEntry.COL_DEV_IP);
        int devicePortIndex = cursor.getColumnIndex(DeviceEntry.COL_DEV_PORT);

        String name = cursor.getString(deviceNameIndex);
        String address = cursor.getString(deviceIpIndex) + ":" + cursor.getInt(devicePortIndex);

        tvName.setText(name);
        tvAddress.setText(address);

    }

}
