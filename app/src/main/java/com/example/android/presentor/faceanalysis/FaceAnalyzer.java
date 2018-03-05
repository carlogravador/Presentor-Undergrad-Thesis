package com.example.android.presentor.faceanalysis;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;

import java.io.IOException;

/**
 * Created by Carlo on 04/03/2018.
 */

public class FaceAnalyzer {

    private Context mContext;
    private FaceDetector mFaceDetector;
    private CameraSource mCameraSource;

    public void start() {
        if (mCameraSource != null) {
            try {
                mCameraSource.start();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        if (mCameraSource != null) {
            mCameraSource.stop();
        }
    }

    public void release() {
        if (mFaceDetector != null) {
            mFaceDetector.release();
        }
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    public void createCameraSource() {
        mFaceDetector = new FaceDetector.Builder(mContext)
                .setProminentFaceOnly(true) // optimize for single, relatively large face
                .setTrackingEnabled(true) // enable face tracking
                .setClassificationType(/* eyes open and smile */ FaceDetector.ALL_CLASSIFICATIONS)
                .setMode(FaceDetector.ACCURATE_MODE) // for one face this is OK
                .build();

        mFaceDetector.setProcessor(new LargestFaceFocusingProcessor(mFaceDetector, new FaceTracker(mContext)));

        if (!mFaceDetector.isOperational()) {
            Log.w("FaceAnalyzer", "createCameraResources: detector NOT operational");
        } else {
            Log.d("FaceAnalyzer", "createCameraResources: detector operational");
        }

        mCameraSource = new CameraSource.Builder(mContext, mFaceDetector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30f)
                .build();

    }

    public FaceAnalyzer(Context context) {
        mContext = context.getApplicationContext();
    }
}
