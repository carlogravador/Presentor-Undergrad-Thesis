package com.example.android.presentor.floatingwidget;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.example.android.presentor.R;
import com.example.android.presentor.screenshare.CreateActivity;
import com.example.android.presentor.screenshare.ShareService;
import com.example.android.presentor.screenshare.ShareService.ServerThread;

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
        ImageButton b;
        switch (view.getId()) {
            case R.id.floating_widget_image_view:
                mFloatingWidgetView.onViewCollapsed();
                break;
            case R.id.circleIv1:    //Stop Button
                //TODO: to do send notification to clients that mirroring is stop
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        stopScreenMirroring();
//                        stopSelf();
//                    }
//                }).start();
                stopSelf();
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
                if (!mServer.getFaceAnalysisMode()) {
                    mServer.setFaceAnalysisMode(true);
                    b.setImageResource(R.drawable.widget_button_icon_face_analysis_clicked);
                } else {
                    mServer.setFaceAnalysisMode(false);
                    b.setImageResource(R.drawable.widget_button_icon_face_analysis);
                }
                break;
            case R.id.circleIv5:    //Domotics Button
                break;
            case R.id.circleIv6:    //Share Button
                if (!CreateActivity.isIsActive()) {
                    Intent i = new Intent(this, CreateActivity.class);
                    startActivity(i);
                }
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
            startActivity(i);
        }
        super.onDestroy();
    }
}
