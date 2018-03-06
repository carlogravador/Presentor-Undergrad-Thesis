package com.example.android.presentor.faceanalysis;

/**
 * Created by Carlo on 04/03/2018.
 */

public class FaceAnalysisActivator {

    private boolean state = false;
    private FaceAnalysisObserver faceAnalysisObserver;

    public interface FaceAnalysisObserver {
        void onFaceAnalysisRequestStateChanged(boolean isChanged);
    }

    public FaceAnalysisActivator() {

    }

    public void setState(boolean b) {
        this.state = b;
        notifyStateChanged(b);
    }

    public void setFaceAnalysisObserver(FaceAnalysisObserver fao){
        this.faceAnalysisObserver = fao;
    }

    public void notifyStateChanged(boolean state){
        faceAnalysisObserver.onFaceAnalysisRequestStateChanged(state);
    }

}
