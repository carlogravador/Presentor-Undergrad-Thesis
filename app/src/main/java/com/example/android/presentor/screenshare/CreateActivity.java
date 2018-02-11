package com.example.android.presentor.screenshare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.KeyListener;
import android.text.method.PasswordTransformationMethod;
import android.transition.Transition;
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
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.android.presentor.R;
import com.example.android.presentor.networkservicediscovery.NsdHelper;
import com.example.android.presentor.utils.ConnectionUtility;
import com.example.android.presentor.utils.Utility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class CreateActivity extends AppCompatActivity {

    public final static String TAG = "CreateActivity";

    private EditText lobbyNameEditText;
    private EditText passwordNameEditText;
    private Button startButton;
    private CheckBox showPassCheckBox;
    private KeyListener kl;

    private int mPort;

    private String mLobbyName;
    private String mLobbyPassword;
    private String creatorName = "Carlo Gravador";

    private static final int REQUEST_CODE = 1000;

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

    ViewGroup rl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        rl = findViewById(R.id.rl);

        lobbyNameEditText = (EditText) findViewById(R.id.edit_text_lobby);
        passwordNameEditText = (EditText) findViewById(R.id.edit_text_password);
        startButton = (Button) findViewById(R.id.button_start_sharing);
        showPassCheckBox = (CheckBox) findViewById(R.id.cb_show_pass);
        kl = lobbyNameEditText.getKeyListener();

        mShareService = ShareService.getInstance();
        mNsdHelper = NsdHelper.getInstatnce();

        mProjectionManager = (MediaProjectionManager) getSystemService
                (Context.MEDIA_PROJECTION_SERVICE);



        if(mShareService.getServerStatus()){

            moveView(RelativeLayout.CENTER_HORIZONTAL, false);

            lobbyNameEditText.setText(Utility.getString(this, this.getResources()
                    .getString(R.string.lobby_name)));
            passwordNameEditText.setText(Utility.getString(this, this.getResources()
                    .getString(R.string.lobby_pass)));
            startButton.setText(this.getResources().getString(R.string.screen_mirror_stop_session));
            etSetEditable(false);
        }

        showPassCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    passwordNameEditText.setTransformationMethod(HideReturnsTransformationMethod
                            .getInstance());
                    passwordNameEditText.setSelection(passwordNameEditText.length());
                }
                else{
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
                    if(TextUtils.isEmpty(lobbyNameEditText.getText())) {
                        lobbyNameEditText.setError("The lobby name cannot be empty");
                        hasError = true;
                    }
                    //check lobbyName if it has special character
                    if(Utility.checkForSpecialCharacter(lobbyNameEditText.getText().toString())){
                        lobbyNameEditText.setError("The lobby name cannot have any special characters.");
                        hasError = true;
                    }
                    //if password is not empty, check if it has special characters
                    if(!TextUtils.isEmpty(passwordNameEditText.getText())){
                        if(Utility.checkForSpecialCharacter(passwordNameEditText.getText()
                                .toString())){
                            passwordNameEditText.setError("The password cannot have any special characters.");
                            hasError = true;
                        }
                    }
                    if(hasError) return;
                    //if it reach this line, no errors
                    startActivityForResult(mProjectionManager.createScreenCaptureIntent(),
                            REQUEST_CODE);
                } else {
                    //else if screen mirroring is already started, then stop
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
        mLobbyName = lobbyNameEditText.getText().toString().trim();
        mLobbyPassword = passwordNameEditText.getText().toString().trim();
        //disables the texfields
        etSetEditable(false);
        //creatorName = ;
        mPort = ConnectionUtility.getPort(this);
        try {
            mShareService.startServer(mPort);
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
        Log.e("CreateActivity", "STOP");
        moveView(RelativeLayout.CENTER_IN_PARENT, true);
        etSetEditable(true);
        deleteLobbyPreferences();
        ConnectionUtility.clearPort(this);
        mShareService.stop();
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


    private void saveLobbyPreferences(String lobbyName, String password, boolean status){
        Utility.saveString(this, this.getResources().getString(R.string.lobby_name), lobbyName);
        Utility.saveString(this, this.getResources().getString(R.string.lobby_pass),password);
        Utility.saveBoolean(this, this.getResources().getString(R.string.lobby_status), status);
    }

    private void deleteLobbyPreferences(){
        Utility.clearKey(this, this.getResources().getString(R.string.lobby_name));
        Utility.clearKey(this, this.getResources().getString(R.string.lobby_pass));
        Utility.clearKey(this, this.getResources().getString(R.string.lobby_status));
    }

    private void etSetEditable(boolean isTrue){
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
    public void moveView(int idPos, boolean withTransition){
        View view = findViewById(R.id.create_center_panel);

        if(withTransition) {
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

    private class ImageAvailableListener implements ImageReader.OnImageAvailableListener {
        public void onImageAvailable(ImageReader imageReader) {
            Image image = null;
            Bitmap bitmap = null;

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
                    bitmap = Bitmap.createBitmap(mDisplayWidth + rowPadding / pixelStride,
                            mDisplayHeight, Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(buffer);

                    //there are clients connected, then send
                    if (mShareService.getClientsStatus()) {
                        byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 5, byteArrayOutputStream);
//                        mShareService.send(byteArrayOutputStream.toByteArray());
                        mShareService.sendToAll(byteArrayOutputStream.toByteArray());
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (bitmap != null) {
                    bitmap.recycle();
                }
                if (image != null) {
                    image.close();
                }
            }
        }
    }

}


