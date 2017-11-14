package com.example.android.presentor.Domotics;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.example.android.presentor.R;

import java.util.List;

/**
 * Created by villa on 13/11/2017.
 */

public class DomoticsSwitchAdapter extends ArrayAdapter<DomoticsSwitch> {

    private final static String LOG_TAG = DomoticsSwitchAdapter.class.getSimpleName();


    public DomoticsSwitchAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<DomoticsSwitch> domoticsSwitches) {
        super(context, 0, domoticsSwitches);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View listItemView = convertView;
        if(listItemView == null){
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_domotics, parent, false);
        }

        DomoticsSwitch domoticsSwitchItem = getItem(position);

//        TextView currentTextView = (TextView)listItemView.findViewById(R.id.text_view_bulb_number);
//        currentTextView.setText(domoticsSwitchItem.getmSwitchName());
//
//        final ImageView imageBulb = (ImageView)listItemView.findViewById(R.id.image_bulb);
//
//        Switch mySwitch = (Switch)listItemView.findViewById(R.id.switch_bulb_status);
//        mySwitch.setChecked(domoticsSwitchItem.ismIsSwitchOn());
//
//        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                GradientDrawable bgShape = (GradientDrawable)imageBulb.getBackground();
//                if(isChecked){
//                    bgShape.setColor(ContextCompat.getColor(getContext(),R.color.bulbColorBackground));
//                }else{
//                    bgShape.setColor(Color.BLACK);
//                }
//            }
//        });




        return listItemView;
    }
}
