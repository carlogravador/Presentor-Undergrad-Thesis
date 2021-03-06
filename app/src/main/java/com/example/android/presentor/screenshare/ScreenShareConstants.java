package com.example.android.presentor.screenshare;

/**
 * Created by Carlo on 07/03/2018.
 */

public interface ScreenShareConstants {

    String BROADCAST_DEVICE_COUNT_CHANGED = "DeviceAdded";
    String BROADCAST_SERVICE_STOP = "stop";

    int SCREEN_PIN_ON = -2;
    int SCREEN_PIN_OFF = -3;
    int FACE_ANALYSIS_ON_NO_SOUNDS = -4;
    int FACE_ANALYSIS_ON_WITH_SOUNDS = -5;
    int FACE_ANALYSIS_OFF = -6;
    int ON_PAUSE = -7;
    int ON_RESUME = -8;
    int ON_STOP = -9;
    int ATTENTION = -10;

}
