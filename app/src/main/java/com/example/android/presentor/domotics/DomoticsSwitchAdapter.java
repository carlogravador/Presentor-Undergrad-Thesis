package com.example.android.presentor.domotics;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.presentor.R;
import com.example.android.presentor.utils.Utility;

import java.util.List;

/**
 * Created by Carlo on 15/11/2017.
 */

public class DomoticsSwitchAdapter extends ArrayAdapter<DomoticsSwitch> {

    String[] applianceNameKey = getContext().getResources().getStringArray(R.array.appliance_name);

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

        CardView cardView = itemView.findViewById(R.id.card_view_domotics);
        final TextView applianceName = itemView.findViewById(R.id.text_view_appliance_name);
        final TextView applianceStatusTextView = itemView.findViewById(R.id.text_view_appliance_status);
        final ImageView applianceStatusImageView = itemView.findViewById(R.id.image_view_appliance_status);
        final Switch applianceStatusSwitch = itemView.findViewById(R.id.switch_appliance);

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
                int i = (int)getItemId(getPosition(ds));
                String key = applianceNameKey[i];
                renameAppliance(getContext(), key, ds);
                return true;
            }
        });

        return itemView;
    }

    public void renameAppliance(final Context context, final String key, final DomoticsSwitch ds){
        AlertDialog.Builder adb = new AlertDialog.Builder(context);
        adb.setTitle("Rename Switch");
        final EditText et = new EditText(context);
        FrameLayout container = new FrameLayout(context);
        FrameLayout.LayoutParams params= new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        int margins = context.getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.setMargins(margins, margins, margins, 0);
        String oldName = Utility.getString(context, key);
        et.setLayoutParams(params);
        et.setText(oldName);
        et.setSelectAllOnFocus(true);
        et.setSingleLine();
        container.addView(et);
        adb.setView(container);

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch(i){
                    case DialogInterface.BUTTON_POSITIVE:
                        //rename appliance method;
                        Utility.saveString(context, key, et.getText().toString().trim());
                        ds.setSwitchName(Utility.getString(context, key));
                        notifyDataSetChanged();

                }
            }
        };


        adb.setPositiveButton(R.string.rename, listener);
        adb.setNegativeButton(R.string.cancel, listener);

        AlertDialog alertDialog = adb.create();
        alertDialog.show();
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


}
