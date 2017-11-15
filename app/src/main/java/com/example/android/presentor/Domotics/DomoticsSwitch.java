package com.example.android.presentor.Domotics;


/**
 * Created by villa on 13/11/2017.
 */

public class DomoticsSwitch {

    private final static String LOG_TAG = DomoticsSwitch.class.getSimpleName();

    private boolean switchStatus;
    private String switchName;


    public boolean getSwitchStatus() {
        return switchStatus;
    }

    public void setSwitchStatus(boolean switchStatus) {
        this.switchStatus = switchStatus;
    }

    public String getSwitchName() {
        return switchName;
    }

    public void setSwitchName(String switchName) {
        this.switchName = switchName;
    }

    public DomoticsSwitch(String switchName, boolean switchStatus) {
        setSwitchName(switchName);
        setSwitchStatus(switchStatus);
    }


}
