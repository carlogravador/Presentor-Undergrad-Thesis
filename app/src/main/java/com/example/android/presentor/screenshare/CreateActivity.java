package com.example.android.presentor.screenshare;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.transition.TransitionManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
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

import com.example.android.presentor.FloatingWidgetService;
import com.example.android.presentor.R;
import com.example.android.presentor.db.DatabaseUtility;
import com.example.android.presentor.db.DevicesCursorAdapter;
import com.example.android.presentor.db.ServicesContract.DeviceEntry;
import com.example.android.presentor.networkservicediscovery.NsdHelper;
import com.example.android.presentor.utils.ConnectionUtility;
import com.example.android.presentor.utils.Utility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class CreateActivity extends AppCompatActivity implements
        LoaderCallbacks<Cursor> {

    public final static String TAG = "CreateActivity";

    private EditText lobbyNameEditText;
    private EditText passwordNameEditText;
    private Button startButton;
    public static TextView deviceCountTv;
    public static ListView connectedDeviceLv;

    //Temporary
    private String creatorName = "Carlo Gravador";

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
    private NsdHelper mNsdHelper;
    private DevicesCursorAdapter mDevicesCursorAdapter;

    ViewGroup rl;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        rl = findViewById(R.id.rl);


        CheckBox showPassCheckBox = (CheckBox) findViewById(R.id.cb_show_pass);

        lobbyNameEditText = (EditText) findViewById(R.id.edit_text_lobby);
        passwordNameEditText = (EditText) findViewById(R.id.edit_text_password);
        startButton = (Button) findViewById(R.id.button_start_sharing);
        deviceCountTv = (TextView) findViewById(R.id.tv_connected_count);
        connectedDeviceLv = (ListView) findViewById(R.id.list_view_devices);


        mShareService = ShareService.getInstance();
        mNsdHelper = NsdHelper.getInstatnce();
        mDevicesCursorAdapter = new DevicesCursorAdapter(this, null);

        mProjectionManager = (MediaProjectionManager) getSystemService
                (Context.MEDIA_PROJECTION_SERVICE);

        connectedDeviceLv.setAdapter(mDevicesCursorAdapter);



        if (mShareService.getServerStatus()) {
            mShareService.setServerActivity(CreateActivity.this);
            moveView(RelativeLayout.CENTER_HORIZONTAL, false);
            lobbyNameEditText.setText(Utility.getString(this, this.getResources()
                    .getString(R.string.lobby_name)));
            passwordNameEditText.setText(Utility.getString(this, this.getResources()
                    .getString(R.string.lobby_pass)));
            startButton.setText(this.getResources().getString(R.string.screen_mirror_stop_session));
            etSetEditable(false);
        }else{
            DatabaseUtility.clearDeviceList(this);
        }

        deviceCountTv.setText("Clients Connected: " + DatabaseUtility.getDeviceCount(this));

        showPassCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if mirroring is not yet started, start it
                if (!mShareService.getServerStatus()) {
                    //check first if the lobbyname is not null
                    boolean hasError = false;
                    if (TextUtils.isEmpty(lobbyNameEditText.getText())) {
                        lobbyNameEditText.setError("The lobby name cannot be empty");
                        hasError = true;
                    }
                    //check lobbyName if it has special character
                    if (Utility.checkForSpecialCharacter(lobbyNameEditText.getText().toString())) {
                        lobbyNameEditText.setError("The lobby name cannot have any special characters.");
                        hasError = true;
                    }
                    //if password is not empty, check if it has special characters
                    if (!TextUtils.isEmpty(passwordNameEditText.getText())) {
                        if (Utility.checkForSpecialCharacter(passwordNameEditText.getText()
                                .toString())) {
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
        });


        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mImageHandler = new Handler();
                Looper.loop();
            }
        }.start();

        getLoaderManager().initLoader(DEVICE_LOADER, null, this);

    }


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
        startScreenSharing(resultCode, data);
    }


    private void createVirtualDisplay() {
        mMediaProjection.createVirtualDisplay("ScreenCapture", mDisplayWidth, mDisplayHeight,
                mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null /*Callbacks*/, mImageHandler
                /*Handler*/);
    }

    private void startScreenSharing(int resultCode, Intent data) {
        startButton.setText(this.getResources().getString(R.string.screen_mirror_stop_session));
        moveView(RelativeLayout.CENTER_HORIZONTAL, true);
        connectedDeviceLv.setVisibility(View.VISIBLE);
        String mLobbyName = lobbyNameEditText.getText().toString().trim();
        String mLobbyPassword = passwordNameEditText.getText().toString().trim();
        //disables the texfields
        etSetEditable(false);

        startService(new Intent(CreateActivity.this, FloatingWidgetService.class));
        //creatorName = ;
        int mPort = ConnectionUtility.getPort(this);
        try {
            mShareService.startServer(CreateActivity.this, mPort);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mNsdHelper.setServiceName(mLobbyName, creatorName, mLobbyPassword);
        mNsdHelper.registerService(mPort);

        saveLobbyPreferences(mLobbyName, mLobbyPassword, mShareService.getServerStatus());

        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mScreenDensity = metrics.densityDpi;
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mDisplayWidth = size.x;
        mDisplayHeight = size.y;

        mImageReader = ImageReader.newInstance(mDisplayWidth, mDisplayHeight, PixelFormat.RGBA_8888,
                2);
        createVirtualDisplay();
        mImageReader.setOnImageAvailableListener(new ImageAvailableListener(), mImageHandler);
    }


    private void stopScreenSharing() {
        startButton.setText(this.getResources().getString(R.string.screen_mirror_start_session));
        moveView(RelativeLayout.CENTER_IN_PARENT, true);
        connectedDeviceLv.setVisibility(View.INVISIBLE);
        DatabaseUtility.clearDeviceList(this);
        deviceCountTv.setText("Client connected: " + DatabaseUtility.getDeviceCount(this));
        etSetEditable(true);
        deleteLobbyPreferences();
        ConnectionUtility.clearPort(this);
        mShareService.stopServer();
        mNsdHelper.stopRegisterService();
        stopMediaProjection();
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
//        if(isTrue){
//            lobbyNameEditText.setKeyListener(kl);
//            passwordNameEditText.setKeyListener(kl);
//        }else{
//            lobbyNameEditText.setKeyListener(null);
//            passwordNameEditText.setKeyListener(null);
//            lobbyNameEditText.clearFocus();
//            passwordNameEditText.clearFocus();
//        }
//        lobbyNameEditText.setFocusable(isTrue);
//        lobbyNameEditText.setFocusableInTouchMode(isTrue);
//        lobbyNameEditText.setClickable(isTrue);
//        passwordNameEditText.setFocusable(isTrue);
//        passwordNameEditText.setFocusableInTouchMode(isTrue);
//        passwordNameEditText.setClickable(isTrue);
        lobbyNameEditText.setEnabled(isTrue);
        passwordNameEditText.setEnabled(isTrue);
        lobbyNameEditText.clearFocus();
        passwordNameEditText.clearFocus();

    }

    //practice
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

    private class ImageAvailableListener implements ImageReader.OnImageAvailableListener {
        public void onImageAvailable(ImageReader imageReader) {
            Image image = null;
            Bitmap bitmap = null;
            Bitmap scaledBm = null;

            ByteArrayOutputStream byteArrayOutputStream;

            try {
                image = imageReader.acquireLatestImage();
                if (image != null) {
                    Image.Plane[] planes = image.getPlanes();
                    ByteBuffer buffer = planes[0].getBuffer();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * mDisplayWidth;

                    //create bitmap
                    bitmap = Bitmap.createBitmap((mDisplayWidth + rowPadding / pixelStride),
                            mDisplayHeight, Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(buffer);

                    //if sharing is paused. dont send any screenshot
                    if(mShareService.onPauseScreenMirroringServer()){
                        return;
                    }
                    //there are clients connected, then send
                    if (mShareService.serverHasClients()) {
                        byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream);
//                        mShareService.send(byteArrayOutputStream.toByteArray());
                        mShareService.sendToAllClientsConnected(byteArrayOutputStream.toByteArray());
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (bitmap != null && scaledBm != null) {
                    bitmap.recycle();
                    scaledBm.recycle();
                }
                if (image != null) {
                    image.close();
                }
            }
        }
    }

}


