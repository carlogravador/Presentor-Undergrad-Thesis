package com.example.android.presentor;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.android.presentor.domotics.DomoticsActivity;
import com.example.android.presentor.screenshare.AccessActivity;
import com.example.android.presentor.screenshare.CreateActivity;
import com.example.android.presentor.screenshare.ShareService;
import com.example.android.presentor.help.HelpActivity;
import com.example.android.presentor.utils.PlayServicesUtil;
import com.example.android.presentor.utils.Utility;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {


    /*  Permission request code to draw over other apps  */
    private static final int DRAW_OVER_OTHER_APP_PERMISSION_REQUEST_CODE = 1222;

    private NavigationView mNavigationView;
    private DrawerLayout mDrawer;
    private Intent mIntent;

    private ShareService mShareService;

    private boolean turnOnBluetooth = false;
    private boolean doubleBackToExitPressedOnce = false;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DRAW_OVER_OTHER_APP_PERMISSION_REQUEST_CODE) {
            //Check if the permission is granted or not.
            if (resultCode == RESULT_OK) {
                //If permission granted start floating widget service
                Log.d("MainActivity", "Permission Granted");
                Utility.showToast(MainActivity.this, "Permission granted");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != Utility.REQUEST_CAMERA_PERM) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        //Camera Permission Granted
        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("MainActivity", "Camera Permission Granted");

            if (!Utility.isFaceAnalysisOperational(MainActivity.this)) {
                Utility.showToast(getApplicationContext(), "Face analysis not operational. ");
                return;
            }
            if (!mShareService.isServerOpen()) {
                Intent i = new Intent(MainActivity.this, AccessActivity.class);
                startActivity(i);
            } else {
                Toast.makeText(MainActivity.this,
                        "Access is not available when Screen Mirroring is running.",
                        Toast.LENGTH_SHORT).show();
            }
            return;
        }

        Utility.showAlertDialog(this,
                false,
                false,
                "Permission Error",
                "Camera permission not granted. Access functionality will not work properly.",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case DialogInterface.BUTTON_POSITIVE:
                                break;
                        }
                    }
                }
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //notification();
        Log.e("MainActivity", "onCreate() callback");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ShareService.getInstance().init(getApplicationContext());
        mShareService = ShareService.getInstance();

        if (!PlayServicesUtil.isPlayServicesAvailable(this, 69)) {
            //TODO: show dialog
            Utility.showToast(this, "PlayServices is not available.");
        }


        CardView shareCardView = findViewById(R.id.card_view_share);
        shareCardView.setOnClickListener(this);

        CardView accessCardView = findViewById(R.id.card_view_access);
        accessCardView.setOnClickListener(this);

        mDrawer = findViewById(R.id.drawer_layout);
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
                    Intent i = new Intent(MainActivity.this, DomoticsActivity.class);
                    Utility.turnOnBluetooth(MainActivity.this, false, i);
                    turnOnBluetooth = false;
                }
                mNavigationView.setCheckedItem(R.id.nav_screen_mirroring);
            }
        });
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.setDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onDestroy() {
        Log.e("MainActivity", "onDestroy() callback");
        mShareService.stop();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        mDrawer = findViewById(R.id.drawer_layout);
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            if (mShareService.isServerOpen()) {
                moveTaskToBack(false);
            } else {
                if (doubleBackToExitPressedOnce) {
                    super.onBackPressed();
                    return;
                }

                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 2000);
            }
            //super.onBackPressed();
        }
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
            case R.id.nav_help:
                mIntent = new Intent(MainActivity.this, HelpActivity.class);
                break;
            case R.id.nav_about:
                mIntent = new Intent(MainActivity.this, AboutActivity.class);
                break;
        }


        mDrawer = findViewById(R.id.drawer_layout);

        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("MainActivity", "onResume callback");
        mNavigationView.setCheckedItem(R.id.nav_screen_mirroring);
        mIntent = null;
        turnOnBluetooth = false;

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.card_view_access:
                if (Utility.isWifiConnected(MainActivity.this)) {
                    // permission granted...?
                    if (!Utility.isCameraPermissionGranted(this)) {
                        //request the camera permission
                        Utility.requestCameraPermission(this);
                    } else {
                        if (!Utility.isFaceAnalysisOperational(MainActivity.this)) {
                            Utility.showToast(getApplicationContext(), "Face analysis not operational. ");
                            return;
                        }
                        if (!mShareService.isServerOpen()) {
                            Intent i = new Intent(MainActivity.this, AccessActivity.class);
                            startActivity(i);
                        } else {
                            Toast.makeText(MainActivity.this,
                                    "Access is not available when Screen Mirroring is running.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    //open dialog box
                    String title = MainActivity.this.getResources()
                            .getString(R.string.screen_share_dialog_title);
                    String message = MainActivity.this.getResources()
                            .getString(R.string.screen_share_dialog_message);
                    Utility.showAlertDialog(MainActivity.this, true,
                            false,
                            title, message,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    switch (i) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                                            break;
                                    }
                                }
                            });
                }
                break;
            case R.id.card_view_share:
                if (Utility.isWifiConnected(MainActivity.this)) {
                    //permission to draw over the app
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                        //If the draw over permission is not available open the settings screen
                        //to grant the permission.
//                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                                Uri.parse("package:" + getPackageName()));
//                        startActivityForResult(intent, DRAW_OVER_OTHER_APP_PERMISSION_REQUEST_CODE);
                        Utility.showAlertDialog(MainActivity.this, true,
                                false,
                                "Permission Error",
                                "Draw over the app permission not granted. Would you like to enable it?",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        switch (i){
                                            case DialogInterface.BUTTON_POSITIVE:
                                                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                        Uri.parse("package:" + getPackageName()));
                                                startActivityForResult(intent, DRAW_OVER_OTHER_APP_PERMISSION_REQUEST_CODE);
                                                break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    Utility.showToast(MainActivity.this, "Please allow necessary permissions.");
                                        }
                                    }
                                });
                    } else {
                        Intent i = new Intent(MainActivity.this, CreateActivity.class);
                        startActivity(i);
                    }
                } else {
                    //open dialog box
                    String title = MainActivity.this.getResources()
                            .getString(R.string.screen_share_dialog_title);
                    String message = MainActivity.this.getResources()
                            .getString(R.string.screen_share_dialog_message);
                    Utility.showAlertDialog(MainActivity.this, true, false,
                            title, message,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    switch (i) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                                            break;
                                    }
                                }
                            });
                }
                break;
        }
    }
}
