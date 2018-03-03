package com.example.android.presentor.observer;



/**
 * Created by Carlo on 01/03/2018.
 */

public class ScreenPinningObservable extends Thread {

    private ScreenPinningObserver screenPinningObserver;

    public interface ScreenPinningObserver {
        void onStateChanged(boolean state);
    }

    private boolean mState = false;


    public void setState(boolean state) {
        mState = state;
        notifyStateChanged(mState);
    }

    public void setScreenPinningObserver(ScreenPinningObserver observer) {
        this.screenPinningObserver = observer;
    }

    public void notifyStateChanged(boolean state){
        screenPinningObserver.onStateChanged(state);
    }


    public ScreenPinningObservable() {
    }

}
