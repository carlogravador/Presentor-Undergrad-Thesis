package com.example.android.presentor;

/**
 * Created by villa on 07/02/2018.
 */

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.android.presentor.screenshare.ShareService;

public class SettingsActivity extends AppCompatActivity {

    private Button a;

    ShareService mShareService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mShareService = ShareService.getInstance();
        a = findViewById(R.id.button);

        a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mShareService.sendCommandToAllClientsConnected(ShareService.FACE_ANALYSIS_ON);
                    }
                }).start();
            }
        });


        final Button pauseButton = (Button) findViewById(R.id.pauseButton);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mShareService.onPauseScreenMirroringServer()) {
                    mShareService.pauseScreenMirroringServer();
                    pauseButton.setText("Resume");
                    a.setVisibility(View.GONE);
                } else {
                    mShareService.resumeScreenMirroringServer();
                    pauseButton.setText("Pause");
                    a.setVisibility(View.VISIBLE);
                }

            }
        });

        final Button pinningButton = (Button) findViewById(R.id.pinningButton);
        pinningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mShareService.getScreenPinningModeServer()) {
                    pinningButton.setText("unpinned");
                } else {
                    pinningButton.setText("pinned");
                }
                new Thread() {
                    @Override
                    public void run() {
                        if (!mShareService.getScreenPinningModeServer()) {
                            mShareService.setScreenPinningModeServer(true);
                            mShareService.sendCommandToAllClientsConnected(ShareService.SCREEN_PIN_ON);
                        } else {
                            mShareService.setScreenPinningModeServer(false);
                            mShareService.sendCommandToAllClientsConnected(ShareService.SCREEN_PIN_OFF);
                        }
                    }
                }.start();
            }
        });

    }

}
