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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.example.android.presentor.R;
import com.example.android.presentor.db.ServicesContract;


public class ClientActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private ShareService mShareService;
    private Uri mCurrentService;
    private final static int CURRENT_SERVICE_LOADER = 0;


    private boolean isInLockTaskMode() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // For SDK version 23 and above.
            return am != null && am.getLockTaskModeState()
                    != ActivityManager.LOCK_TASK_MODE_NONE;
        }

        // When SDK version >= 21. This API is deprecated in 23.
        return am != null && am.isInLockTaskMode() &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }



    @Override
    public void onBackPressed() {
        if (isInLockTaskMode()) {
            return;
        }
        //disconnects the client
        mShareService.disconnectClient();
        super.onBackPressed();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        ImageView imageView = findViewById(R.id.image_view_screen_share);
        imageView.setKeepScreenOn(true);

        mShareService = ShareService.getInstance();

        Intent i = getIntent();
        mCurrentService = i.getData();

        getLoaderManager().initLoader(CURRENT_SERVICE_LOADER, null, this);

    }

    @Override
    protected void onDestroy() {
        Log.e("ClientActivity", "onDestroy() callback");
        super.onDestroy();
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
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int ipIndex = cursor.getColumnIndex(ServicesContract.ServiceEntry.COL_IP_ADDRESS);
            int portIndex = cursor.getColumnIndex(ServicesContract.ServiceEntry.COL_PORT_NUMBER);

            final String ip = cursor.getString(ipIndex);
            final int port = cursor.getInt(portIndex);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    mShareService.connectClient(ClientActivity.this, ip, port);
                }
            }).start();

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
