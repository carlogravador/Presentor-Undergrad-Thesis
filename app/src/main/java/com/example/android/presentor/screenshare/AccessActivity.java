package com.example.android.presentor.screenshare;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.example.android.presentor.R;
import com.example.android.presentor.db.DatabaseUtility;
import com.example.android.presentor.db.ServiceCursorAdapter;
import com.example.android.presentor.db.ServicesContract.ServiceEntry;
import com.example.android.presentor.networkservicediscovery.NsdHelper;

import java.util.Timer;
import java.util.TimerTask;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

public class AccessActivity extends AppCompatActivity
        implements LoaderCallbacks<Cursor> {

    private final static int SERVICE_LOADER = 0;

    private NsdHelper mNsdHelper;
    private ServiceCursorAdapter mServiceCursorAdapter;

    public static PulsatorLayout pulsator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access);


        pulsator = (PulsatorLayout) findViewById(R.id.pulsator);

        //delete first existing database every time activity is created
        DatabaseUtility.clearServiceList(this);

        //create new database;
        mServiceCursorAdapter = new ServiceCursorAdapter(this, null);
        mNsdHelper = NsdHelper.getInstatnce();
        initClientSide();


        ListView lobbyListView = (ListView) findViewById(R.id.list_view_lobby);
        lobbyListView.setAdapter(mServiceCursorAdapter);
        lobbyListView.setEmptyView(pulsator);



        getLoaderManager().initLoader(SERVICE_LOADER, null, this);

    }


    @Override
    protected void onStop() {
        mNsdHelper.stopDiscovery();
        super.onStop();
    }




    @Override
    protected void onResume() {
        super.onResume();
        DatabaseUtility.clearServiceList(this);
        pulsator.start();
        ShareService.getInstance().disconnectClient();
        new Timer().schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        mNsdHelper.discoverServices();
                    }
                }, 2000
        );
    }

    private void initClientSide() {
        new Timer().schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        mNsdHelper.initClientSide();
                    }
                }, 2000
        );
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This loader will execute the ContentProvider's query method on a background thread
        String[] projection = {
                ServiceEntry._ID,
                ServiceEntry.COL_SERVICE_NAME,
                ServiceEntry.COL_CREATOR_NAME,
                ServiceEntry.COL_PASSWORD,
                ServiceEntry.COL_IP_ADDRESS,
                ServiceEntry.COL_PORT_NUMBER
        };

        return new CursorLoader(this,
                ServiceEntry.CONTENT_URI_SERVICE,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mServiceCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mServiceCursorAdapter.swapCursor(null);
    }


}
