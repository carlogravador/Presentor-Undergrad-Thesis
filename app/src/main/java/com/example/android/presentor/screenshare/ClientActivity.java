package com.example.android.presentor.screenshare;

import android.app.ActivityManager;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageView;

import com.example.android.presentor.R;
import com.example.android.presentor.db.ServicesContract;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

public class ClientActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    ImageView mImageView;
    Handler mUiHandler;
    ShareService mShareService;

    Uri mCurrentService;
    public final static int CURRENT_SERVICE_LOADER = 0;


    public boolean isInLockTaskMode(){
        ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // For SDK version 23 and above.
            return am.getLockTaskModeState()
                    != ActivityManager.LOCK_TASK_MODE_NONE;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // When SDK version >= 21. This API is deprecated in 23.
            return am.isInLockTaskMode();
        }
        return false;
    }


    class ConnectThread extends Thread {
        String ip;
        int port;

        public ConnectThread(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        @Override
        public void run() {
            mShareService.connectClient(ClientActivity.this, ip, port);
        }
    }


    @Override
    public void onBackPressed() {
        if(isInLockTaskMode()){
            return;
        }
        super.onBackPressed();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        //DatabaseUtility.clearServiceList(this);
        PulsatorLayout pl = AccessActivity.pulsator;
        pl.stop();


        mShareService = ShareService.getInstance();
        mUiHandler = new Handler();
        mImageView = (ImageView) findViewById(R.id.image_view_screen_share);
        mImageView.setKeepScreenOn(true);

        Intent i = getIntent();
        mCurrentService = i.getData();

        getLoaderManager().initLoader(CURRENT_SERVICE_LOADER, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ServicesContract.ServiceEntry._ID,
                ServicesContract.ServiceEntry.COL_IP_ADDRESS,
                ServicesContract.ServiceEntry.COL_PORT_NUMBER
        };
        return new CursorLoader(this,
                mCurrentService,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(cursor == null || cursor.getCount() < 1){
            return;
        }
        if(cursor.moveToFirst()){
                int ipIndex = cursor.getColumnIndex(ServicesContract.ServiceEntry.COL_IP_ADDRESS);
                int portIndex = cursor.getColumnIndex(ServicesContract.ServiceEntry.COL_PORT_NUMBER);

                String ip = cursor.getString(ipIndex);
                int port = cursor.getInt(portIndex);

                Log.d("Client Activity", "Load Finish: " + ip + "/" + port);
                new ConnectThread(ip, port).start();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void startLockTask() {
        Log.e("ClientActivity", "startLockTask() called");
        super.startLockTask();
    }

    @Override
    public void stopLockTask() {
        Log.e("ClientActivity", "stopLockTask() called");
        super.stopLockTask();
    }

}
