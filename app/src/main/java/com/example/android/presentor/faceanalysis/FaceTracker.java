package com.example.android.presentor.faceanalysis;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;

import com.example.android.presentor.R;
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
    private boolean mHasSound;

    private MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;

    private Handler vibrateHandler;

    private Context mContext;
    private Vibrator vibrator;

    private Thread vibrateIndefinitelyThread;

    private Runnable vibrateEvent;

    AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                    focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                // AUDIOFOCUS_LOSS TRANSIENT means we have lost audio focus for a short amount of time
                // and AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK means we have lost audio focus
                // our app still continues to play song at lower volume but in both cases,
                // we want our app to pause playback and start it from beginning.
                mMediaPlayer.pause();
                mMediaPlayer.seekTo(0);
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                // it means we have gained focused and start playback
                mMediaPlayer.start();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                // it means we have completely lost the focus and we
                // have to stop the playback and free up the playback resources
                releaseMediaPlayer();
            }
        }
    };

    public FaceTracker(Context context, boolean hasSound) {
        mContext = context;
        vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mHasSound = hasSound;
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
                Utility.showToast(mContext, "Please pay attention to the presentation.");
                if (mHasSound) {
                    playSound();
                }
                while (attentionLost) {
                    if (!isVibrateStarted) {
                        vibrator.vibrate(100000000);
                        isVibrateStarted = true;
                    }
                }
                releaseMediaPlayer();
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

    private void playSound() {
        // relase the media player object if currently exist because we are going to change the song
        releaseMediaPlayer();

        // Request audio focus so in order to play the audio file. The app needs to play a
        // short audio file, so we will request audio focus with a short amount of time
        // with AUDIOFOCUS_GAIN_TRANSIENT.
        int result = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //we have the audio focus now

            // creates new media player object
            mMediaPlayer = MediaPlayer.create(mContext, R.raw.notify);

            /**
             * set on completion listener on the mediaplayer object
             * and relase media player object as soon song stops playing*/
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {

                    // now the sound file has finished player, so free up the media player resources
                    releaseMediaPlayer();
                }
            });

            mMediaPlayer.start();
        }
    }


    /**
     * Clean up the media player by releasing its resources.
     */
    private void releaseMediaPlayer() {
        // If the media player is not null, then it may be currently playing a sound.
        if (mMediaPlayer != null) {
            // Regardless of the current state of the media player, release its resources
            // because we no longer need it.
            mMediaPlayer.release();

            // Set the media player back to null. For our code, we've decided that
            // setting the media player to null is an easy way to tell that the media player
            // is not configured to play an audio file at the moment.
            mMediaPlayer = null;

            // Regardless of whether or not we were granted audio focus, abandon it. This also
            // unregisters the AudioFocusChangeListener so we don't get anymore callbacks.
            mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
        }
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
        releaseMediaPlayer();
    }


}
