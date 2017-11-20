package com.example.android.presentor.screenshare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.presentor.R;
import com.example.android.presentor.networkservicediscovery.NsdHelper;
import com.example.android.presentor.utils.ConnectionUtility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class CreateActivity extends AppCompatActivity {

    public final static String TAG = "CreateActivity";

    private EditText lobbyNameEditText;
    private EditText passwordNameEditText;
    private Button startButton;

    private static int mPort;

    private String mLobbyName;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        lobbyNameEditText = (EditText) findViewById(R.id.edit_text_lobby);
        passwordNameEditText = (EditText) findViewById(R.id.edit_text_password);
        startButton = (Button) findViewById(R.id.button_start_sharing);

        mShareService = new ShareService();
        mNsdHelper = new NsdHelper(this);

        mProjectionManager = (MediaProjectionManager) getSystemService
                (Context.MEDIA_PROJECTION_SERVICE);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mShareService.isServerOpen) {
                    //start
                    startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
                } else {
                    //stop
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
        mLobbyName = lobbyNameEditText.getText().toString().trim();
        //creatorName = ;
        mPort = ConnectionUtility.getPort(this);
        try {
            mShareService.start(mPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mNsdHelper.setServiceName(mLobbyName, creatorName);
        mNsdHelper.registerService(mPort);

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

                    if (mShareService.hasClients) {
                        byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 5, byteArrayOutputStream);
                        mShareService.send(byteArrayOutputStream.toByteArray());
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


