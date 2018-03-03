package com.example.android.presentor;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
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
import android.widget.Toast;

import com.example.android.presentor.domotics.DomoticsActivity;
import com.example.android.presentor.networkservicediscovery.NsdHelper;
import com.example.android.presentor.screenshare.AccessActivity;
import com.example.android.presentor.screenshare.CreateActivity;
import com.example.android.presentor.screenshare.ShareService;
import com.example.android.presentor.utils.Utility;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    NavigationView mNavigationView;
    DrawerLayout mDrawer;
    Intent mIntent;

    boolean turnOnBluetooth = false;
    boolean doubleBackToExitPressedOnce = false;


    ShareService mShareSrvice;

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

        //notification();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        NsdHelper.getInstatnce().init(getApplicationContext());
        ShareService.getInstance().init(getApplicationContext());

        mShareSrvice = ShareService.getInstance();

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
                    if (!mShareSrvice.getServerStatus()) {
                        Intent i = new Intent(MainActivity.this, AccessActivity.class);
                        startActivity(i);
                    } else {
                        Toast.makeText(MainActivity.this,
                                "Access is not available when Screen Mirroring is running.",
                                Toast.LENGTH_SHORT).show();
                    }
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
                } else if (turnOnBluetooth) {
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
            if(mShareSrvice.getServerStatus()){
                MainActivity.this.moveTaskToBack(true);
            }else{
                if (doubleBackToExitPressedOnce) {
                    super.onBackPressed();
                    return;
                }

                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce=false;
                    }
                }, 2000);
            }
            //super.onBackPressed();
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
                if (Utility.isBluetoothOn()) {
                    mIntent = new Intent(MainActivity.this, DomoticsActivity.class);
//                    new BtConnectAsyncTask(this).execute();
                } else {
                    turnOnBluetooth = true;
                }
                break;
            case R.id.nav_settings:
                mIntent = new Intent(MainActivity.this, SettingsActivity.class);
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



    //TODO : BUG: multiple instances of app opens when application is clicked followed by clicking the notification
    NotificationCompat.Builder notification;
    private static final int uniqueID = 45612;



    public void startNotification(){
        //Build the notification

        notification = new NotificationCompat.Builder(this);

        Bitmap logo = BitmapFactory.decodeResource(getResources(),R.drawable.ic_splash_logo);
        notification.setLargeIcon(logo);
        notification.setSmallIcon(R.drawable.ic_notif_icon);



        //notification.setTicker("This is the ticker");
        //notification.setWhen(System.currentTimeMillis());
        notification.setContentTitle("Presentor is running");
        notification.setContentText("Tap for more details.");
        notification.setOngoing(true);
        notification.setAutoCancel(false);


        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(pendingIntent);

        //Builds notification and issues it
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(uniqueID, notification.build());

    }




}
