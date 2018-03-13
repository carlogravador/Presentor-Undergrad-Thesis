package com.example.android.presentor.db;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.android.presentor.R;
import com.example.android.presentor.db.ServicesContract.ServiceEntry;
import com.example.android.presentor.screenshare.ClientActivity;
import com.example.android.presentor.utils.Utility;



/**
 * Created by Carlo on 17/10/2017.
 */

public class ServiceCursorAdapter extends CursorAdapter {

    private Context mContext;

    public ServiceCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_lobby, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        CardView cardView = view.findViewById(R.id.card_view_lobby);
        TextView textViewServiceName = view.findViewById(R.id.text_view_lobby_name);
        TextView textViewServiceCreator = view.findViewById(R.id.text_view_creator_name);
        ImageView imageViewBackground = view.findViewById(R.id.list_item_background);
        ImageView imageViewSecuredIndicator = view.findViewById(R.id.iv_secured_indicator);


        int serviceNameIndex = cursor.getColumnIndex(ServiceEntry.COL_SERVICE_NAME);
        int serviceCreatorIndex = cursor.getColumnIndex(ServiceEntry.COL_CREATOR_NAME);
        int servicePasswordIndex = cursor.getColumnIndex(ServiceEntry.COL_PASSWORD);
        int ipIndex = cursor.getColumnIndex(ServiceEntry.COL_IP_ADDRESS);
        int portIndex = cursor.getColumnIndex(ServiceEntry.COL_PORT_NUMBER);

        GradientDrawable backgroundCircle = (GradientDrawable)imageViewBackground.getBackground();
        final String serviceName = cursor.getString(serviceNameIndex);
        final String serviceCreator = cursor.getString(serviceCreatorIndex);
        final String servicePassword = cursor.getString(servicePasswordIndex);
        String creatorIp = cursor.getString(ipIndex);
        int port = cursor.getInt(portIndex);

        final String address = creatorIp + ":" + port;


        final int position = cursor.getPosition();


        textViewServiceName.setText(serviceName);
        textViewServiceCreator.setText(serviceCreator);
        backgroundCircle.setColor(getColor(getItemId(cursor.getPosition())));

        if(servicePassword.length() == 0){
            imageViewSecuredIndicator.setVisibility(View.GONE);
        }

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ClientActivity.class);
                Uri uri = ContentUris.withAppendedId(ServiceEntry.CONTENT_URI_SERVICE, getItemId(position));
                intent.setData(uri);

                Utility.showConnectDialog(context, intent, serviceName, serviceCreator, address,
                            servicePassword);

            }
        });

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
