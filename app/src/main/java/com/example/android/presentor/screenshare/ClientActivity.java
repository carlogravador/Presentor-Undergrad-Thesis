package com.example.android.presentor.screenshare;

import android.app.ActivityManager;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.presentor.R;
import com.example.android.presentor.db.ServicesContract;
import com.example.android.presentor.utils.Utility;


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

    private String getPrefName() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPrefs.getString(getResources().getString(R.string.pref_player_key),
                "Player");
    }


    @Override
    public void onBackPressed() {
        if (isInLockTaskMode()) {
            return;
        }

        super.onBackPressed();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);


        mShareService = ShareService.getInstance();

        Intent i = getIntent();
        mCurrentService = i.getData();

        getLoaderManager().initLoader(CURRENT_SERVICE_LOADER, null, this);

    }

    @Override
    protected void onDestroy() {
        Log.e("ClientActivity", "onDestroy() callback");
        //disconnects the client
        mShareService.disconnectClient();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
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
            TextView tv = (TextView) findViewById(R.id.message_tv);
            tv.setText("Error connecting to the server.");
            tv.setBackgroundColor(getResources().getColor(R.color.colorRed));
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
                    mShareService.connectClient(ClientActivity.this, ip, port, getPrefName());
                }
            }).start();

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

}
