package com.example.android.presentor.faceanalysis;

/**
 * Created by Carlo on 04/03/2018.
 */

public class FaceAnalysisActivator {
    private FaceAnalysisObserver faceAnalysisObserver;

    public interface FaceAnalysisObserver {
        void onFaceAnalysisRequestStateChanged(boolean isChanged, boolean hasSound);
    }

    public FaceAnalysisActivator() {

    }

    public void setState(boolean b, boolean hasSound) {
        notifyStateChanged(b, hasSound);
    }

    public void setFaceAnalysisObserver(FaceAnalysisObserver fao){
        this.faceAnalysisObserver = fao;
    }

    private void notifyStateChanged(boolean state, boolean hasSound){
        faceAnalysisObserver.onFaceAnalysisRequestStateChanged(state, hasSound);
    }

}
