package com.example.android.presentor;

/**
 * Created by villa on 07/02/2018.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity{

    private Button a;
    private TextView b;



    Dialog customDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        customDialog = new Dialog(this);
//        customDialog.setContentView(R.layout.create_lobby_custom_dialog);
//        customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        a = (Button) findViewById(R.id.button);
        a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                customDialog.setContentView(R.layout.create_lobby_custom_dialog);
                customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                customDialog.show();
            }
        });

        b = (TextView) findViewById(R.id.textView);
    }


//    @Override
//    public void onClick(View v) {
//        // TODO Auto-generated method stub
//        Dialog customDialog = new Dialog(getApplicationContext());
//
//        customDialog.setContentView(R.layout.create_lobby_custom_dialog);
//        customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        customDialog.show();
//    }
}
