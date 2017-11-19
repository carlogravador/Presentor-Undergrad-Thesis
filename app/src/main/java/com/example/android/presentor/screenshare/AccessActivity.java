package com.example.android.presentor.screenshare;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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
        implements LoaderCallbacks<Cursor>{

    private final static int SERVICE_LOADER = 0;

    private NsdHelper mNsdHelper;
    private ServiceCursorAdapter mServiceCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access);

        PulsatorLayout mPulsator;
        mPulsator = (PulsatorLayout)findViewById(R.id.pulsator);
        mPulsator.start();

        //delete first existing database everytime activity is created
        //DatabaseUtility.dbCheck();
        DatabaseUtility.clearServiceList(this);
        //create new database;
        mServiceCursorAdapter = new ServiceCursorAdapter(this, null);
        mNsdHelper = new NsdHelper(this);
        initClientSide();


        ListView lobbyListView = (ListView)findViewById(R.id.list_view_lobby);
        lobbyListView.setAdapter(mServiceCursorAdapter);
        lobbyListView.setEmptyView(mPulsator);

        lobbyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Intent intent = new Intent(AccessActivity.this, ClientActivity.class);

            }
        });

        getLoaderManager().initLoader(SERVICE_LOADER, null, this);

    }

    private void initClientSide(){
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
                ServiceEntry.COL_CREATOR_NAME
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