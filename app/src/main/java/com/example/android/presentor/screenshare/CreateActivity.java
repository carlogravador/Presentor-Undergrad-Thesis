package com.example.android.presentor.screenshare;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.transition.TransitionManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.presentor.MainActivity;
import com.example.android.presentor.R;
import com.example.android.presentor.db.DatabaseUtility;
import com.example.android.presentor.db.DevicesCursorAdapter;
import com.example.android.presentor.db.ServicesContract.DeviceEntry;
import com.example.android.presentor.floatingwidget.FloatingWidgetService;
import com.example.android.presentor.networkservicediscovery.NsdHelper;
import com.example.android.presentor.utils.ConnectionUtility;
import com.example.android.presentor.utils.Utility;
import com.example.android.presentor.screenshare.ShareService.ServerThread;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class CreateActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener,
        View.OnClickListener, LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {

    public final static String TAG = "CreateActivity";
    private static boolean isActive = false;

    public static boolean isIsActive() {
        return isActive;
    }


    private EditText lobbyNameEditText;
    private EditText passwordNameEditText;
    private Button startButton;
    private TextView deviceCountTv;
    private ListView connectedDeviceLv;
    private ViewGroup rl;

    private int mCompressQuality;
    private float mResizeRatio;


    //Temporary
    private String creatorName;

    private static final int REQUEST_CODE = 1000;
    private static final int DEVICE_LOADER = 0;

    private int mScreenDensity;
    private int mDisplayWidth;
    private int mDisplayHeight;
    //private int mImageProduced;


    private ImageReader mImageReader;
    private MediaProjection mMediaProjection;
    private MediaProjectionManager mProjectionManager;
    private Handler mImageHandler;

    private ShareService mShareService;
    private ServerThread mServer;
    private NsdHelper mNsdHelper;
    private DevicesCursorAdapter mDevicesCursorAdapter;

    private ImageAvailableListener mImageGenerator;


    private BroadcastReceiver mBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ScreenShareConstants.BROADCAST_DEVICE_COUNT_CHANGED:
                        //TODO: add string literals to strings.xml use place holder
                        deviceCountTv.setText("Clients Connected: " +
                                DatabaseUtility.getDeviceCount(getApplicationContext()));
                        break;
                    case ScreenShareConstants.BROADCAST_SERVICE_STOP:
                        stopEvent();
                    default:
                        break;
                }
            }
        }
    };

    private void getSharedPrefValues() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        creatorName = sharedPrefs.getString(getResources().getString(R.string.pref_player_key),
                "Player");

        mCompressQuality = Integer.parseInt(
                sharedPrefs.getString(getResources().getString(R.string.pref_mirroring_quality_key),
                        "75"));

        mResizeRatio = Float.parseFloat(
                sharedPrefs.getString(getResources().getString(R.string.pref_mirroring_resize_key),
                        "75"));
        mResizeRatio = mResizeRatio / 100;
    }


    private void createVirtualDisplay() {
        mMediaProjection.createVirtualDisplay("ScreenCapture", mDisplayWidth, mDisplayHeight,
                mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null /*Callbacks*/, mImageHandler
                /*Handler*/);
    }

    private void stopMediaProjection() {
        mImageHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mMediaProjection != null) {
                    mMediaProjection.stop();
                }
            }
        });
    }

    private void startScreenSharing(int resultCode, Intent data) {
        startService(new Intent(CreateActivity.this, FloatingWidgetService.class));
        //UI updates
        getSharedPrefValues();
        startButton.setText(this.getResources().getString(R.string.screen_mirror_stop_session));
        moveView(RelativeLayout.CENTER_HORIZONTAL, true);
        connectedDeviceLv.setVisibility(View.VISIBLE);
        String lobbyName = lobbyNameEditText.getText().toString().trim();
        String lobbyPassword = passwordNameEditText.getText().toString().trim();
        //disables the texfields
        etSetEditable(false);
        //creatorName = ;
        int mPort = ConnectionUtility.getPort(this);
        try {
            mServer = mShareService.startServer(mPort);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mNsdHelper.setServiceName(lobbyName, creatorName, lobbyPassword);
        mNsdHelper.registerService(mPort);

        saveLobbyPreferences(lobbyName, lobbyPassword, mShareService.isServerOpen());

        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mScreenDensity = metrics.densityDpi;
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        mDisplayWidth = size.x;
        mDisplayHeight = size.y;


        mImageReader = ImageReader.newInstance(mDisplayWidth, mDisplayHeight, PixelFormat.RGBA_8888,
                2);
        createVirtualDisplay();
        mImageReader.setOnImageAvailableListener(new ImageAvailableListener(), mImageHandler);
    }

    private void stopScreenSharing() {
        //stop the service
        mNsdHelper.stopRegisterService();
        mShareService.stopServer();
        stopService(new Intent(CreateActivity.this, FloatingWidgetService.class));
    }

    private void stopEvent() {
        mNsdHelper.stopRegisterService();
        stopMediaProjection();

        DatabaseUtility.clearDeviceList(this);
        deleteLobbyPreferences();
        ConnectionUtility.clearPort(this);

        //UI updates
        startButton.setText(this.getResources().getString(R.string.screen_mirror_start_session));
        moveView(RelativeLayout.CENTER_IN_PARENT, true);
        connectedDeviceLv.setVisibility(View.INVISIBLE);
        //TODO: add string literals to strings.xml use placeholder
        deviceCountTv.setText("Clients connected: " + DatabaseUtility.getDeviceCount(this));
        etSetEditable(true);
    }

    private void saveLobbyPreferences(String lobbyName, String password, boolean status) {
        Utility.saveString(this, this.getResources().getString(R.string.lobby_name), lobbyName);
        Utility.saveString(this, this.getResources().getString(R.string.lobby_pass), password);
        Utility.saveBoolean(this, this.getResources().getString(R.string.lobby_status), status);
    }

    private void deleteLobbyPreferences() {
        Utility.clearKey(this, this.getResources().getString(R.string.lobby_name));
        Utility.clearKey(this, this.getResources().getString(R.string.lobby_pass));
        Utility.clearKey(this, this.getResources().getString(R.string.lobby_status));
    }

    private void etSetEditable(boolean isTrue) {
        lobbyNameEditText.clearFocus();
        passwordNameEditText.clearFocus();
        lobbyNameEditText.setEnabled(isTrue);
        passwordNameEditText.setEnabled(isTrue);
    }

    public void moveView(int idPos, boolean withTransition) {
        View view = findViewById(R.id.create_center_panel);

        if (withTransition) {
            TransitionManager.beginDelayedTransition(rl);
        }

        RelativeLayout.LayoutParams positionRules = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        positionRules.addRule(idPos, RelativeLayout.TRUE);
        int margins = this.getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        int top = this.getResources().getDimensionPixelSize(R.dimen.top_margin);
        positionRules.setMargins(margins, top, margins, margins);
        view.setLayoutParams(positionRules);
    }

    //-----------------------------------Activity methods----------------------------------------//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        Log.e("CreateActivity", "onCreate() callback");

        rl = (ViewGroup) findViewById(R.id.rl);

        CheckBox showPassCheckBox = (CheckBox) findViewById(R.id.cb_show_pass);

        lobbyNameEditText = (EditText) findViewById(R.id.edit_text_lobby);
        passwordNameEditText = (EditText) findViewById(R.id.edit_text_password);
        startButton = (Button) findViewById(R.id.button_start_sharing);
        deviceCountTv = (TextView) findViewById(R.id.tv_connected_count);
        connectedDeviceLv = (ListView) findViewById(R.id.list_view_devices);

        mDevicesCursorAdapter = new DevicesCursorAdapter(this, null);

        mProjectionManager = (MediaProjectionManager) getSystemService
                (Context.MEDIA_PROJECTION_SERVICE);

        mShareService = ShareService.getInstance();
        mNsdHelper = NsdHelper.getInstance();

        if (mShareService.isServerOpen()) {
            moveView(RelativeLayout.CENTER_HORIZONTAL, false);

            lobbyNameEditText.setText(Utility.getString(this, this.getResources()
                    .getString(R.string.lobby_name)));
            passwordNameEditText.setText(Utility.getString(this, this.getResources()
                    .getString(R.string.lobby_pass)));
            startButton.setText(this.getResources().getString(R.string.screen_mirror_stop_session));
            etSetEditable(false);
        }

        connectedDeviceLv.setAdapter(mDevicesCursorAdapter);


        showPassCheckBox.setOnCheckedChangeListener(this);
        startButton.setOnClickListener(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                mImageHandler = new Handler();
                Looper.loop();
            }
        }).start();

        getLoaderManager().initLoader(DEVICE_LOADER, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActive = true;
        Log.e("CreateActivity", "onResume() callback");
        //TODO: add string literals to strings.xml use place holder
        IntentFilter filter = new IntentFilter();
        filter.addAction(ScreenShareConstants.BROADCAST_DEVICE_COUNT_CHANGED);
        filter.addAction(ScreenShareConstants.BROADCAST_SERVICE_STOP);
        LocalBroadcastManager.getInstance(getApplicationContext()).
                registerReceiver(mBroadCastReceiver, filter);
        LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(new Intent(ScreenShareConstants.BROADCAST_DEVICE_COUNT_CHANGED));

        //update UI
        if (!mShareService.isServerOpen()) {
            stopEvent();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActive = false;
        Log.e("CreateActivity", "onPause() callback");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadCastReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
//        mNsdHelper.stopRegisterService();
//        mShareService.stopServer();
        Log.e("CreateActivity", "onDestroy() callback");
        isActive = false;
        super.onDestroy();
    }

//    @Override
//    public void onBackPressed() {
//        if (mServer != null && mServer.getStatus()) {
//            moveTaskToBack(true);
//        } else {
//            super.onBackPressed();
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE) {
            Log.e(TAG, "Unknown request code: " + requestCode);
            return;
        }
        if (resultCode != RESULT_OK) {
            Toast.makeText(this,
                    "Screen Cast Permission Denied", Toast.LENGTH_SHORT).show();
            return;
        }
        //start screen sharing
        startScreenSharing(resultCode, data);
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

    //------------------------LoaderManger.LoaderCallbacks Implementation------------------------//

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This loader will execute the ContentProvider's query method on a background thread
        String[] projection = {
                DeviceEntry._ID,
                DeviceEntry.COL_DEV_NAME,
                DeviceEntry.COL_DEV_IP,
                DeviceEntry.COL_DEV_PORT
        };

        return new CursorLoader(this,
                DeviceEntry.CONTENT_URI_DEVICE,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mDevicesCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mDevicesCursorAdapter.swapCursor(null);
    }


    //------------------------View.OnClickListener Implementation--------------------------------//
    @Override
    public void onClick(View view) {
        //if mirroring is not yet started, start it
        if (!mShareService.isServerOpen()) {
            //check first if the lobbyname is not null
            boolean hasError = false;
            if (TextUtils.isEmpty(lobbyNameEditText.getText())) {
                //TODO: add string literals to strings.xml
                lobbyNameEditText.setError("The lobby name cannot be empty");
                hasError = true;
            }
            //check lobbyName if it has special character
            if (Utility.checkForSpecialCharacter(lobbyNameEditText.getText().toString())) {
                //TODO: add string literals to strings.xml
                lobbyNameEditText.setError("The lobby name cannot have any special characters.");
                hasError = true;
            }
            //if password is not empty, check if it has special characters
            if (!TextUtils.isEmpty(passwordNameEditText.getText())) {
                if (Utility.checkForSpecialCharacter(passwordNameEditText.getText()
                        .toString())) {
                    //TODO: add string literals to strings.xml
                    passwordNameEditText.setError("The password cannot have any special characters.");
                    hasError = true;
                }
            }
            if (hasError) return;
            //if it reach this line, no errors
            startActivityForResult(mProjectionManager.createScreenCaptureIntent(),
                    REQUEST_CODE);
        } else {
            //else if screen mirroring is already started, then stopServer
            stopScreenSharing();
        }
    }

    //---------------------CompoundButton.OnCheckedChangeListener Implementation-------------------//
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (isChecked) {
            passwordNameEditText.setTransformationMethod(HideReturnsTransformationMethod
                    .getInstance());
            passwordNameEditText.setSelection(passwordNameEditText.length());
        } else {
            passwordNameEditText.setTransformationMethod(PasswordTransformationMethod
                    .getInstance());
            passwordNameEditText.setSelection(passwordNameEditText.length());
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.e("CreateActivity", "onSharedPreferenceChanged() callback");

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (key.equals(getResources().getString(R.string.pref_mirroring_quality_key))) {
            mCompressQuality = Integer.parseInt(sharedPrefs.getString(key, "75"));
        } else if (key.equals(getResources().getString(R.string.pref_mirroring_resize_key))) {
            mResizeRatio = Float.parseFloat(sharedPrefs.getString(key, "75"));
            mResizeRatio = mResizeRatio / 100;
        }

    }


    //----------------------------ImageAvailableListener SubClass---------------------------------//

    private class ImageAvailableListener implements ImageReader.OnImageAvailableListener {

        Bitmap reusableBitmap = null;

        private Bitmap resizeBitmap(Bitmap cleanBitmap, float ratio) {
            int origWidth = cleanBitmap.getWidth();
            int origHeight = cleanBitmap.getHeight();

            //get the values in settings
            Matrix scaleMatrix = new Matrix();
            scaleMatrix.preScale(ratio, ratio);

            return Bitmap.createBitmap(cleanBitmap, 0, 0, origWidth, origHeight, scaleMatrix, true);
        }

        @Override
        public void onImageAvailable(ImageReader imageReader) {

            //conditions are satisfied, proceed to create bitmap and send.
            Image image = null;

            Bitmap cleanBitmap = null;
            Bitmap resizeBitmap = null;

            ByteArrayOutputStream byteArrayOutputStream;

            try {
                image = imageReader.acquireLatestImage();
                if (image != null) {
                    Image.Plane[] planes = image.getPlanes();
                    ByteBuffer buffer = planes[0].getBuffer();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * mDisplayWidth;


                    //TODO: width and height coming from the shared preference
                    int width = (mDisplayWidth + rowPadding / pixelStride);
                    int height = mDisplayHeight;

                    if(width > image.getWidth()){
                        if(reusableBitmap == null){
                            reusableBitmap = Bitmap.createBitmap(width, image.getHeight(), Bitmap.Config.ARGB_8888);
                        }
                        reusableBitmap.copyPixelsFromBuffer(buffer);
                        cleanBitmap = Bitmap.createBitmap(reusableBitmap, 0, 0 , image.getWidth(), image.getHeight());
                    }else{
                        cleanBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                        //or
                        //cleanBitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
                        cleanBitmap.copyPixelsFromBuffer(buffer);
                    }
//
//                    //create bitmap
//                    cleanBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//                    //or
//                    //cleanBitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
//                    cleanBitmap.copyPixelsFromBuffer(buffer);

                    //try only
                    if (mResizeRatio == 1) {
                        resizeBitmap = cleanBitmap;
                    } else {
                        resizeBitmap = resizeBitmap(cleanBitmap, mResizeRatio);
                        cleanBitmap.recycle();
                    }

                    //if sharing is paused. then don't send
                    if (mServer.isPausedScreenMirroring()) {
                        return;
                    }
                    //there are clients connected, then send
                    if (mServer.hasClients()) {
                        byteArrayOutputStream = new ByteArrayOutputStream();
                        resizeBitmap.compress(Bitmap.CompressFormat.JPEG, mCompressQuality, byteArrayOutputStream);
                        mServer.sendToAll(byteArrayOutputStream.toByteArray());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (resizeBitmap != null) {
                    resizeBitmap.recycle();
                }
                if (image != null) {
                    image.close();
                }
            }
        }
    }
}


