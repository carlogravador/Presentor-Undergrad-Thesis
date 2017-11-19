package com.example.android.presentor.db;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.android.presentor.R;
import com.example.android.presentor.db.ServicesContract.ServiceEntry;

import java.util.concurrent.ThreadLocalRandom;


/**
 * Created by Carlo on 17/10/2017.
 */

public class ServiceCursorAdapter extends CursorAdapter {

    Context mContext;

    public ServiceCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_lobby, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView textViewServiceName = (TextView) view.findViewById(R.id.text_view_lobby_name);
        TextView textViewServiceCreator = (TextView) view.findViewById(R.id.text_view_creator_name);
        ImageView imageViewBackground = (ImageView) view.findViewById(R.id.list_item_background);

        int serviceNameIndex = cursor.getColumnIndex(ServiceEntry.COL_SERVICE_NAME);
        int serviceCreatorIndex = cursor.getColumnIndex(ServiceEntry.COL_CREATOR_NAME);

        GradientDrawable backgroundCircle = (GradientDrawable)imageViewBackground.getBackground();
        String serviceName = cursor.getString(serviceNameIndex);
        String serviceCreator = cursor.getString(serviceCreatorIndex);




        textViewServiceName.setText(serviceName);
        textViewServiceCreator.setText(serviceCreator);
        backgroundCircle.setColor(getColor(getItemId(cursor.getPosition())));

    }

    private int getColor(long id) {
        int magnitudeColorResourceId;
        int color = (int)id % 8;
        switch (color) {
            case 1:
                magnitudeColorResourceId = R.color.colorBlue;

                break;
            case 2:
                magnitudeColorResourceId = R.color.colorOrange;
                break;
            case 3:
                magnitudeColorResourceId = R.color.colorGray;
                break;
            case 4:
                magnitudeColorResourceId = R.color.colorViolet;

                break;
            case 5:
                magnitudeColorResourceId = R.color.colorRed;
                break;
            case 6:
                magnitudeColorResourceId = R.color.colorIndigo;
                break;
            case 7:
                magnitudeColorResourceId = R.color.colorGreen;
                break;
            default:
                magnitudeColorResourceId = R.color.colorDefault;
        }

        return ContextCompat.getColor(mContext, magnitudeColorResourceId);
    }
}