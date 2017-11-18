package com.example.android.presentor.screenshare;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.android.presentor.R;
import com.example.android.presentor.db.ServicesContract;

import java.net.InetAddress;

public class ClientActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    ImageView mImageView;
    Handler mUiHandler;
    ShareService mShareService;

    Uri mCurrentService;
    public final static int CURRENT_SERVICE_LOADER = 0;


    class ConnecThread extends Thread {
        InetAddress inetAddress;
        int port;

        public ConnecThread(InetAddress inetAddress, int port) {
            this.inetAddress = inetAddress;
            this.port = port;
        }

        @Override
        public void run() {
            mShareService.connect(inetAddress, port, mUiHandler, mImageView);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        Intent i = getIntent();
        mCurrentService = getIntent().getData();

        getLoaderManager().initLoader(CURRENT_SERVICE_LOADER, null, this);

        mImageView = (ImageView) findViewById(R.id.image_view_screen_share);

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

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
