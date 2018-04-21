package com.example.android.presentor.help;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.presentor.MainActivity;
import com.example.android.presentor.R;


public class DomoticsSlide extends AppCompatActivity {

    public static final String SPAWN_BY_THIS_ACTIVITY = "domoticsSlide";

    private ViewPager mSlideViewPager;
    private LinearLayout mDotLayout;

    private TextView[] mDots;

    private SliderAdapterDomotics sliderAdapterDomotics;

    private Button mNextBtn;
    private Button mBackBtn;
    private Button mSkipBtn;

    private int mCurrentPage;
    private boolean mSpawnOnMainActivity;
    private boolean mSpawnOnPreviousHelp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_domotics);

        Intent i = getIntent();
        mSpawnOnMainActivity = i.getBooleanExtra(MainActivity.SPAWN_ON_MAIN_ACTIVITY, false);
        mSpawnOnPreviousHelp = i.getBooleanExtra(DomoticsSlide.SPAWN_BY_THIS_ACTIVITY, false);

        mSlideViewPager = (ViewPager) findViewById(R.id.slideViewPager);
        mDotLayout = (LinearLayout) findViewById(R.id.dotsLayout);

        mBackBtn = (Button) findViewById(R.id.prevBtn);
        mNextBtn = (Button) findViewById(R.id.nextBtn);
        mSkipBtn = (Button) findViewById(R.id.skipBtn);

        sliderAdapterDomotics = new SliderAdapterDomotics(this);

        mSlideViewPager.setAdapter(sliderAdapterDomotics);
        mSlideViewPager.addOnPageChangeListener(viewListener);


        mDots = new TextView[9];
        if (mSpawnOnPreviousHelp) {
            mCurrentPage = mDots.length - 1;
        }
        if(mSpawnOnMainActivity){
            mBackBtn.setText("Back");
            mBackBtn.setEnabled(true);
            mBackBtn.setVisibility(View.VISIBLE);
        }
        addDotsIndicator(mCurrentPage);
        mSlideViewPager.setCurrentItem(mCurrentPage);

        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNextBtn.getText().equals("Finish")) {
                    finish();
                }
                mSlideViewPager.setCurrentItem(mCurrentPage + 1);

            }
        });

        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSpawnOnMainActivity && mCurrentPage == 0) {
                    Intent i = new Intent(DomoticsSlide.this, WidgetButtonsSlide.class);
                    i.putExtra(MainActivity.SPAWN_ON_MAIN_ACTIVITY, mSpawnOnMainActivity);
                    i.putExtra(SPAWN_BY_THIS_ACTIVITY, true);
                    startActivity(i);
                    finish();
                }
                mSlideViewPager.setCurrentItem(mCurrentPage - 1);
            }
        });

        mSkipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();

            }
        });

    }

    public void addDotsIndicator(int position) {

        mDotLayout.removeAllViews();

        for (int i = 0; i < mDots.length; i++) {

            mDots[i] = new TextView(this);
            mDots[i].setText(Html.fromHtml("&#8226;"));
            mDots[i].setTextSize(35);
            mDots[i].setTextColor(getResources().getColor(R.color.colorTransparentWhite));

            mDotLayout.addView(mDots[i]);

        }

        if (mDots.length > 0) {

            mDots[position].setTextColor(getResources().getColor(R.color.colorWhite));
        }
        mDotLayout.bringToFront();
    }

    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int i) {

            addDotsIndicator(i);
            mCurrentPage = i;

            if (i == 0) {
                mNextBtn.setEnabled(true);
                mNextBtn.setText("Next");
                if (mSpawnOnMainActivity) {
                    mBackBtn.setText("Back");
                    mBackBtn.setVisibility(View.VISIBLE);
                    mBackBtn.setEnabled(true);
                } else {
                    mBackBtn.setText("");
                    mBackBtn.setEnabled(false);
                    mBackBtn.setVisibility(View.INVISIBLE);
                }
            } else if (i == mDots.length - 1) {

                mNextBtn.setEnabled(true);
                mBackBtn.setEnabled(true);
                mBackBtn.setVisibility(View.VISIBLE);

                mNextBtn.setText("Finish");
                mBackBtn.setText("Back");

            } else {

                mNextBtn.setEnabled(true);
                mBackBtn.setEnabled(true);
                mBackBtn.setVisibility(View.VISIBLE);

                mNextBtn.setText("Next");
                mBackBtn.setText("Back");

            }


        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

}

