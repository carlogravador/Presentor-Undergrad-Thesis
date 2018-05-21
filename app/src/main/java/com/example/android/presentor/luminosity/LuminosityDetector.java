package com.example.android.presentor.luminosity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class LuminosityDetector implements SensorEventListener {


    private SensorManager mSensorManager;
    private Sensor lightSensor;

    private float mLuminosityValue;

    public LuminosityDetector(Context context) {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        lightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        mSensorManager.registerListener(this,
                lightSensor,
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    public float getLuminosityValue() {
        return mLuminosityValue;
    }

    public void releaseDetector() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        mLuminosityValue = sensorEvent.values[0];
        Log.e("LuminosityDetector", "Luminosity: " + mLuminosityValue + " lx");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
//        if(sensor == lightSensor){
//            Log.e("LuminosityDetector", "onAccuracy changed")
//        }
        Log.e("LuminosityDetector", "onAccuracy changed: " + i);
    }
}
