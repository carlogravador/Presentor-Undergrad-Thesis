package com.example.android.presentor.floatingwidget;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.example.android.presentor.R;
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
                if (!mServer.isPausedScreenMirroring()) mServer.pauseScreenMirroring();
                else mServer.resumeScreenMirroring();
                break;
            case R.id.circleIv3:    //Screen Pinning Button
                if (!mServer.getScreenPinningModeServer()) mServer.setScreenPinningMode(true);
                else mServer.setScreenPinningMode(false);
                break;
            case R.id.circleIv4:    //Face Analysis Button
                if (!mServer.getFaceAnalysisMode()) mServer.setFaceAnalysisMode(true);
                else mServer.setFaceAnalysisMode(false);
                break;
            case R.id.circleIv5:    //Domotics Button
                break;
            case R.id.circleIv6:    //Share Button
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
        super.onDestroy();
    }
}
