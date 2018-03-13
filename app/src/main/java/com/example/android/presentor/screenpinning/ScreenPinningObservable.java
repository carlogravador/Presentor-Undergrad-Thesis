package com.example.android.presentor.screenpinning;



/**
 * Created by Carlo on 01/03/2018.
 */

public class ScreenPinningObservable extends Thread {

    private ScreenPinningObserver screenPinningObserver;

    public interface ScreenPinningObserver {
        void onStateChanged(boolean state);
    }


    public void setState(boolean state) {
        notifyStateChanged(state);
    }

    public void setScreenPinningObserver(ScreenPinningObserver observer) {
        this.screenPinningObserver = observer;
    }

    private void notifyStateChanged(boolean state){
        screenPinningObserver.onStateChanged(state);
    }


    public ScreenPinningObservable() {
    }

}
