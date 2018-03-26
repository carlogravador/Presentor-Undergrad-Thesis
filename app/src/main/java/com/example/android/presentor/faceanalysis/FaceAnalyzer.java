package com.example.android.presentor.faceanalysis;

import android.content.Context;
import android.os.Vibrator;
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
            } catch (IOException | SecurityException e) {
                e.printStackTrace();
            }
        }
    }


    public void release() {
        if (mCameraSource != null) {
            mCameraSource.stop();
            mCameraSource.release();
        }
        if (mFaceDetector != null) {
            mFaceDetector.release();
        }

//        ((Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE)).cancel();
    }

    public void createCameraSource() {
        mFaceDetector = new FaceDetector.Builder(mContext)
                .setProminentFaceOnly(true) // optimize for single, relatively large face
                .setTrackingEnabled(true) // enable face tracking
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS) /* eyes open and smile */
                .setMode(FaceDetector.ACCURATE_MODE) // to get EulerY value
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
