package com.example.android.presentor;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.widget.CardView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.presentor.domotics.DomoticsActivity;
import com.example.android.presentor.screenshare.AccessActivity;
import com.example.android.presentor.screenshare.CreateActivity;
import com.example.android.presentor.utils.Utility;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    NavigationView mNavigationView;
    DrawerLayout mDrawer;
    Intent mIntent;

    boolean turnOnBluetooth = false;


    DialogInterface.OnClickListener dialoagNetworkListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            switch (i) {
                case DialogInterface.BUTTON_POSITIVE:
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    break;
            }
        }
    };


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CardView shareCardView = (CardView) findViewById(R.id.card_view_share);
        shareCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utility.isWifiConnected(MainActivity.this)) {
                    Intent i = new Intent(MainActivity.this, CreateActivity.class);
                    startActivity(i);
                } else {
                    //open dialog box
                    String title = MainActivity.this.getResources()
                            .getString(R.string.screen_share_dialog_title);
                    String message = MainActivity.this.getResources()
                            .getString(R.string.screen_share_dialog_message);
                    Utility.showAlertDialog(MainActivity.this, title, message,
                            dialoagNetworkListener);
                }
            }
        });

        CardView accessCardView = (CardView) findViewById(R.id.card_view_access);
        accessCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utility.isWifiConnected(MainActivity.this)) {
                    Intent i = new Intent(MainActivity.this, AccessActivity.class);
                    startActivity(i);
                } else {
                    //open dialog box
                    String title = MainActivity.this.getResources()
                            .getString(R.string.screen_share_dialog_title);
                    String message = MainActivity.this.getResources()
                            .getString(R.string.screen_share_dialog_message);
                    Utility.showAlertDialog(MainActivity.this, title, message,
                            dialoagNetworkListener);
                }
            }
        });

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                //Set your new fragment here
                if (mIntent != null) {
                    startActivity(mIntent);
                    //added
                    mIntent = null;
                }else if(turnOnBluetooth){
                    mNavigationView.setCheckedItem(R.id.nav_screen_mirroring);
                    Utility.turnOnBluetooth(MainActivity.this);
                    turnOnBluetooth = false;
                }
//                if(turnOnBluetooth){
//                    Utility.turnOnBluetooth(MainActivity.this);
//                    turnOnBluetooth = false;
//                }
                mNavigationView.setCheckedItem(R.id.nav_screen_mirroring);
            }
        });
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.setDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_screen_mirroring:
                mIntent = null;
                break;
            case R.id.nav_domotics:
                if(Utility.isBluetoothOn()){
                   mIntent = new Intent(MainActivity.this, DomoticsActivity.class);
//                    new BtConnectAsyncTask(this).execute();
                }
                else{
                    turnOnBluetooth = true;
                }
                break;
            case R.id.nav_settings:
                mIntent = null;
                break;
            case R.id.nav_about:
                mIntent = new Intent(MainActivity.this, AboutActivity.class);
                break;
        }


        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        mNavigationView.setCheckedItem(R.id.nav_screen_mirroring);
        mIntent = null;
        turnOnBluetooth = false;
        super.onResume();

    }
}
