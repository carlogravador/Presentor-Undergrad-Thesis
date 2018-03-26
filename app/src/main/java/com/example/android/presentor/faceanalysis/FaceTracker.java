package com.example.android.presentor.faceanalysis;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;

import com.example.android.presentor.utils.Utility;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;

/**
 * Created by Carlo on 04/03/2018.
 */

public class FaceTracker extends Tracker<Face> {

    private static final float PROB_THRESHOLD = .2f;
    private boolean attentionLost;
    private boolean hasFace;
    private boolean handlerStarting;
    private boolean isVibrateStarted;

    private Handler vibrateHandler;

    private Context mContext;
    private Vibrator vibrator;

    private Thread vibrateIndefinitelyThread;

    private Runnable vibrateEvent;

    public FaceTracker(Context context) {
        mContext = context;
        vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        prepareVibrateEvent();
    }

    private void prepareVibrateEvent() {
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                vibrateHandler = new Handler();
                Looper.loop();
            }
        }.start();


        vibrateEvent = new Runnable() {
            @Override
            public void run() {
                //Utility.showToast(mContext, "End of Timer, start vibrating.");
                vibrateIndefinitelyThread.run();
            }
        };

        vibrateIndefinitelyThread = new Thread() {
            @Override
            public void run() {
                while (attentionLost) {
                    if (!isVibrateStarted) {
                        vibrator.vibrate(100000000);
                        isVibrateStarted = true;
                    }
                }
                vibrator.cancel();
                isVibrateStarted = false;
            }
        };
    }

    private void startHandler() {
        vibrateHandler.postDelayed(vibrateEvent, 5000);
        handlerStarting = true;
    }

    private void stopHandler() {
        vibrateHandler.removeCallbacks(vibrateEvent);
        handlerStarting = false;
    }

    @Override
    public void onUpdate(Detector.Detections<Face> detections, Face face) {
        if (hasFace) {
            if (!(face.getEulerY() >= -12 && face.getEulerY() <= 12)) {
                //face is not front facing, attention has been lost
                if (!handlerStarting) {  //start the handler if it's still not starting
                    startHandler();
                }
                attentionLost = true;
            } else {
//                //face is front facing, check if eyes are opened
//                Log.e("FaceTracker", "Left eye proba = " + face.getIsLeftEyeOpenProbability()
//                        + " and Right eye proba = " + face.getIsRightEyeOpenProbability());
                boolean isEyesClosed = face.getIsLeftEyeOpenProbability() <= PROB_THRESHOLD &&
                            face.getIsLeftEyeOpenProbability() != -1.f &&
                        face.getIsRightEyeOpenProbability() <= PROB_THRESHOLD &&
                        face.getIsRightEyeOpenProbability() != -1.f;

                if (isEyesClosed) {
                    //no attention
                    //Log.e("FaceTracker", "Eyes is Closed");
                    //Utility.showToast(mContext, "Eyes is Closed");
                    attentionLost = true;
                    if (!handlerStarting) {
                        startHandler();
                    }
                } else {
                    //Log.e("FaceTracker", "Attention regained");
                    attentionLost = false;
                    if (handlerStarting) {
                        stopHandler();
                    }
                }
            }
        }

    }

    @Override
    public void onNewItem(int i, Face face) {
        //Log.e("FaceTracker", "onNewItem() callback");
        //Utility.showToast(mContext, "Face detected");
        hasFace = true;
    }

    @Override
    public void onMissing(Detector.Detections<Face> detections) {
        //Log.e("FaceTracker", "onMissing() callback");
        hasFace = false;
        if (!handlerStarting) {
            startHandler();
            attentionLost = true;
        }
    }

    @Override
    public void onDone() {
        //Log.e("FaceTracker", "onDone() callback");
        attentionLost = false;
        stopHandler();
    }


}
