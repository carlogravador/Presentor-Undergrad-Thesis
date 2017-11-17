package com.example.android.presentor.screenshare;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.example.android.presentor.R;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

public class AccessActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access);

        PulsatorLayout mPulsator;
        mPulsator = (PulsatorLayout)findViewById(R.id.pulsator);
        mPulsator.start();

        //ListView lobbyListView = (ListView)findViewById(R.id.list_view_lobby);

    }
}
