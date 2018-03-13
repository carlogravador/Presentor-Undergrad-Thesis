package com.example.android.presentor.faceanalysis;

/**
 * Created by Carlo on 04/03/2018.
 */

public class FaceAnalysisActivator {
    private FaceAnalysisObserver faceAnalysisObserver;

    public interface FaceAnalysisObserver {
        void onFaceAnalysisRequestStateChanged(boolean isChanged);
    }

    public FaceAnalysisActivator() {

    }

    public void setState(boolean b) {
        notifyStateChanged(b);
    }

    public void setFaceAnalysisObserver(FaceAnalysisObserver fao){
        this.faceAnalysisObserver = fao;
    }

    private void notifyStateChanged(boolean state){
        faceAnalysisObserver.onFaceAnalysisRequestStateChanged(state);
    }

}
