package com.example.android.presentor.domotics;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.presentor.R;

import java.util.List;

/**
 * Created by Carlo on 15/11/2017.
 */

public class DomoticsSwitchAdapter extends ArrayAdapter<DomoticsSwitch> {


    public DomoticsSwitchAdapter(@NonNull Context context, int resource, @NonNull List<DomoticsSwitch> domoticsSwitches) {
        super(context, 0, domoticsSwitches);
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_domotics, parent, false);
        }

        final DomoticsSwitch ds = getItem(position);

        CardView cardView = (CardView) itemView.findViewById(R.id.card_view_domotics);
        final TextView applianceName = (TextView) itemView.findViewById(R.id.text_view_appliance_name);
        final TextView applianceStatusTextView = (TextView) itemView.findViewById(R.id.text_view_appliance_status);
        final ImageView applianceStatusImageView = (ImageView) itemView.findViewById(R.id.image_view_appliance_status);
        final Switch applianceStatusSwitch = (Switch) itemView.findViewById(R.id.switch_appliance);

        applianceName.setText(ds.getSwitchName());
        if (ds.getSwitchStatus()) {
            applianceStatusTextView.setText(getContext().getResources().
                    getString(R.string.appliance_status_on));
        } else {
            applianceStatusTextView.setText(getContext().getResources().
                    getString(R.string.appliance_status_off));
        }

        applianceStatusSwitch.setChecked(ds.getSwitchStatus());

        applianceStatusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                Drawable d;
                if (isChecked) {
                    ds.setSwitchStatus(true);
                    if(!DomoticsActivity.sArduinoSwitchesState[position]) {
                        DomoticsActivity.turnOnArduinoSwitch(position, true);
                    }
                    d = getContext().getResources().getDrawable(R.drawable.ic_power_bg_on);
                    applianceStatusTextView.setText(getContext().getResources().
                            getString(R.string.appliance_status_on));
                    if (isAllSwitchOpen()) {
                        DomoticsActivity.sMasterSwitch.setChecked(true);
                    }
                } else {
                    ds.setSwitchStatus(false);
                    if(DomoticsActivity.sArduinoSwitchesState[position]) {
                        DomoticsActivity.turnOnArduinoSwitch(position, false);
                    }
                    d = getContext().getResources().getDrawable(R.drawable.ic_power_bg_off);
                    applianceStatusTextView.setText(getContext().getResources().
                            getString(R.string.appliance_status_off));
                    DomoticsActivity.sIsAllSwitchOpen = false;
                    DomoticsActivity.sMasterSwitch.setChecked(false);
                }
                applianceStatusImageView.setBackground(d);
            }
        });

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                applianceStatusSwitch.setChecked(!applianceStatusSwitch.isChecked());
            }
        });

        cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                //TODO by Dan add Dialogbox that has a textfield and Fix navigation bar
                AlertDialog ad = builder.create();
                ad.show();

                Log.d("DomoticsSwitchAdapter", "Long press click: " + applianceName);
                return true;
            }
        });

        return itemView;
    }

    private boolean isAllSwitchOpen() {
        for (int i = 0; i < 8; i++) {
            boolean somethingIsOff = !getItem(i).getSwitchStatus();
            if (somethingIsOff) {
                return false;
            }
        }
        return true;
    }

    AlertDialog.Builder builder = new AlertDialog.Builder(getContext()){

        EditText editText = new EditText(DomoticsSwitchAdapter.this.getContext());

        @Override
        public AlertDialog.Builder setView(View view) {
            view = editText;
            return super.setView(view);
        }

        DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d("DomoticsSwitchAdapter", "Positive Button Click");
            }
        };

        DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d("DomoticsSwitchAdapter", "Negative Button CLick");
            }
        };

        public AlertDialog.Builder setPositiveButton(CharSequence text, DialogInterface.OnClickListener listener) {
            listener = positiveListener;
            return super.setPositiveButton(text, listener);
        }

        @Override
        public AlertDialog.Builder setNegativeButton(CharSequence text, DialogInterface.OnClickListener listener) {
            listener = negativeListener;
            return super.setNegativeButton(text, listener);
        }

        @Override
        public AlertDialog.Builder setTitle(CharSequence title) {
            title = "AWWWW";
            return super.setTitle(title);
        }
    };

}
