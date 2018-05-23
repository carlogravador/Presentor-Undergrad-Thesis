package com.example.android.presentor.floatingwidget;

import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.example.android.presentor.MainActivity;
import com.example.android.presentor.R;
import com.example.android.presentor.domotics.DomoticsActivity;
import com.example.android.presentor.domotics.DomoticsSelectActivity;
import com.example.android.presentor.screenshare.CreateActivity;
import com.example.android.presentor.screenshare.ScreenShareConstants;
import com.example.android.presentor.screenshare.ShareService;
import com.example.android.presentor.screenshare.ShareService.ServerThread;
import com.example.android.presentor.utils.Utility;

/**
 * Created by Carlo on 10/03/2018.
 */

public class FloatingWidgetService extends Service implements View.OnClickListener {

    private FloatingWidgetView mFloatingWidgetView;
    private ShareService mShareService;
    private ServerThread mServer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mShareService = ShareService.getInstance();
        mServer = mShareService.getServer();
        mFloatingWidgetView = new FloatingWidgetView(getApplicationContext(), this);

    }

    @Override
    public void onClick(View view) {
        final ImageButton b;
        switch (view.getId()) {
            case R.id.circleIv1:    //Stop Button
                //TODO: to do send notification to clients that mirroring is stop
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        stopScreenMirroring();
//                        stopSelf();
//                    }
//                }).start();
                Utility.showAlertDialog(this,
                        true,
                        true,
                        "Stop Screen mirroring",
                        "Are you sure you want to stop screen mirroring?",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        stopSelf();
                                        break;
                                }
                            }
                        });
//                stopSelf();
                break;
            case R.id.circleIv2:    //Pause Button
                b = mFloatingWidgetView.getImageButton(1);
                if (!mServer.isPausedScreenMirroring()) {
                    mServer.pauseScreenMirroring();
                    b.setImageResource(R.drawable.widget_button_icon_play_screenshare);
                } else {
                    mServer.resumeScreenMirroring();
                    b.setImageResource(R.drawable.widget_button_icon_pause_screenshare);
                }
                break;
            case R.id.circleIv3:    //Screen Pinning Button
                b = mFloatingWidgetView.getImageButton(2);
                if (!mServer.getScreenPinningModeServer()) {
                    mServer.setScreenPinningMode(true);
                    b.setImageResource(R.drawable.widget_button_icon_screen_pinned);
                } else {
                    mServer.setScreenPinningMode(false);
                    b.setImageResource(R.drawable.widget_button_icon_screen_pin);
                }
                break;
            case R.id.circleIv4:    //Face Analysis Button
                b = mFloatingWidgetView.getImageButton(3);
                if (mServer.getFaceAnalysisMode() == ScreenShareConstants.FACE_ANALYSIS_OFF) {
//                    mServer.setFaceAnalysisMode(true);
//                    b.setImageResource(R.drawable.widget_button_icon_face_analysis_clicked);
                    Utility.showFaceAnalysisModeDialog(this,
                            "Face Analysis Activation",
                            "Face Analysis will be activated, please select mode.",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    switch (i) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            mServer.setFaceAnalysisMode(ScreenShareConstants.FACE_ANALYSIS_ON_NO_SOUNDS);
                                            b.setImageResource(R.drawable.widget_button_icon_face_analysis_clicked);
                                            break;
                                        case DialogInterface.BUTTON_NEGATIVE:
                                            mServer.setFaceAnalysisMode(ScreenShareConstants.FACE_ANALYSIS_ON_WITH_SOUNDS);
                                            b.setImageResource(R.drawable.widget_button_icon_face_analysis_clicked);
                                            break;
                                    }
                                }
                            });
                } else {
                    mServer.setFaceAnalysisMode(ScreenShareConstants.FACE_ANALYSIS_OFF);
                    b.setImageResource(R.drawable.widget_button_icon_face_analysis);
                }
                break;
            case R.id.circleIv5:    //Domotics Button
                if (Utility.isBluetoothOn()) {
                    if(Utility.getBoolean(getApplicationContext(), getResources().getString(R.string.pref_auto_connect_key))) {
                        if(!DomoticsActivity.isActivityOpen){
                            Intent i = new Intent(this, DomoticsActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);
                        }
                    }else{
                        if(!DomoticsSelectActivity.isActivityOpen){
                            Intent i = new Intent(this, DomoticsSelectActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);
                        }
                    }
                } else {
                    Intent i = new Intent(this, DomoticsActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Utility.turnOnBluetooth(this ,
                            "Domotics requires bluetooth connection. \n\n" +
                            "Do you want to open bluetooth now?",
                            true,
                            i);
                }
                break;
            case R.id.circleIv6:    //Share Button
                if (!CreateActivity.isIsActive()) {
                    Intent i = new Intent(this, CreateActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }
                break;
            case R.id.circleIv7: //attention button
                mServer.sendAttentionCommand();
                break;
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mFloatingWidgetView.handleOrientationChanges(newConfig);
    }

    @Override
    public void onDestroy() {
        Log.e("FloatingWidgetService", "onDestroy() callback");
        mFloatingWidgetView.removeView();
        mShareService.stopServer();
        if (!CreateActivity.isIsActive()) {
            Intent i = new Intent(this, CreateActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
        super.onDestroy();
    }
}
