package com.example.android.presentor.Domotics;

/**
 * Created by villa on 13/11/2017.
 */

public class DomoticsSwitch {
    private final static String LOG_TAG = DomoticsSwitch.class.getSimpleName();

    private boolean mIsSwitchOn;
    private String mSwitchName;

    public boolean ismIsSwitchOn() {
        return mIsSwitchOn;
    }

    public void setmIsSwitchOn(boolean mIsSwitchOn) {
        this.mIsSwitchOn = mIsSwitchOn;
    }

    public String getmSwitchName() {
        return mSwitchName;
    }

    public void setmSwitchName(String mSwitchName) {
        this.mSwitchName = mSwitchName;
    }

    public DomoticsSwitch(boolean mIsSwitchOn, String mSwitchName) {
        this.mIsSwitchOn = mIsSwitchOn;
        this.mSwitchName = mSwitchName;
    }
}
