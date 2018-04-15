package com.example.android.presentor.help;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.example.android.presentor.R;

public class HelpActivity extends AppCompatActivity {

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        String[] helpSlide = {"Screen Mirroring", "Widget Buttons", "Domotics"};
        ListAdapter helpAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, helpSlide);
        ListView helpListView = (ListView) findViewById(R.id.helpList);
        helpListView.setAdapter(helpAdapter);

         helpListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch((int)id){
                    case 0:
                        Intent i = new Intent(HelpActivity.this, ScreenMirroringSlide.class);
                        startActivity(i);
                        break;
                    case 1:
                        Intent j = new Intent(HelpActivity.this, WidgetButtonsSlide.class);
                        startActivity(j);
                        break;

                    case 2:
                        Intent k = new Intent(HelpActivity.this, DomoticsSlide.class);
                        startActivity(k);
                        break;
                }
            }
        });


    }

}
