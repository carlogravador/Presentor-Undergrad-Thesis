package com.example.android.presentor.floatingwidget;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.transition.TransitionManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.example.android.presentor.R;
import com.example.android.presentor.screenshare.ShareService;

/**
 * Created by Carlo on 05/03/2018.
 */

public class FloatingWidgetService extends Service implements View.OnClickListener {

    private WindowManager mWindowManager;
    private ShareService mShareService;

    private View mFloatingWidgetView, collapsedView, expandedView;
    private Point szWindow = new Point();
    private ImageButton[] arrayImageButton;

    private int x_init_cord, y_init_cord, x_init_margin, y_init_margin;

    //Variable to check if the Floating widget view is on left side or in right side
    // initially we are displaying Floating widget view to Left side so set it to true
    private boolean isLeft = true;

    public FloatingWidgetService() {

    }

    private void addFloatingWidgetView(LayoutInflater inflater) {
        //Inflate the floating view layout we created
        mFloatingWidgetView = inflater.inflate(R.layout.floating_widget_layout, null);

        //Add the view to the window.
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,      //TYPE_APLICATION_OVERLAY
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT);

        //Specify the view position
        params.gravity = Gravity.TOP | Gravity.LEFT;

        //Initially view will be added to top-left corner, you change x-y coordinates according to your need
        params.x = -10;
        params.y = 100;

        //Add the view to the window
        mWindowManager.addView(mFloatingWidgetView, params);


        //find id of collapsed view layout
        collapsedView = mFloatingWidgetView.findViewById(R.id.collapse_view);

        //find id of the expanded view layout
        expandedView = mFloatingWidgetView.findViewById(R.id.expanded_container);
        arrayImageButton = new ImageButton[6];
        arrayImageButton[0] = (ImageButton) expandedView.findViewById(R.id.circleIv1);
        arrayImageButton[1] = (ImageButton) expandedView.findViewById(R.id.circleIv2);
        arrayImageButton[2] = (ImageButton) expandedView.findViewById(R.id.circleIv3);
        arrayImageButton[3] = (ImageButton) expandedView.findViewById(R.id.circleIv4);
        arrayImageButton[4] = (ImageButton) expandedView.findViewById(R.id.circleIv5);
        arrayImageButton[5] = (ImageButton) expandedView.findViewById(R.id.circleIv6);
    }

    private void getWindowManagerDefaultDisplay() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int h = displayMetrics.heightPixels;
        int w = displayMetrics.widthPixels;
        szWindow.set(w, h);
    }

    private void showButtons() {
        //to make the width of expanded view match the screen
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) mFloatingWidgetView.getLayoutParams();
        params.width = szWindow.x;
        mFloatingWidgetView.setLayoutParams(params);
        mWindowManager.updateViewLayout(mFloatingWidgetView, params);

        ViewGroup vg = (ViewGroup) expandedView;
        TransitionManager.beginDelayedTransition(vg);

        int subButton = getResources().getDimensionPixelSize(R.dimen.sub_button_dimen);
        int marginLeft = getResources().getDimensionPixelSize(R.dimen.margin_to_logo);
        int marginAllowance = getResources().getDimensionPixelSize(R.dimen.margin_allowance);


        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) arrayImageButton[0].getLayoutParams();
        lp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        lp.setMargins(marginLeft, 0, 0, 0);
        arrayImageButton[0].setLayoutParams(lp);


        for (int i = 1; i < arrayImageButton.length; i++) {
            ImageButton ib = arrayImageButton[i];
            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) ib.getLayoutParams();
            int marginLeftIncrement = marginLeft + (i * (subButton + marginAllowance));
            rlp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            rlp.setMargins(marginLeftIncrement, 0, 0, 0);
            ib.setLayoutParams(rlp);
        }

    }

    private void hideButtons() {
        ViewGroup vg = (ViewGroup) expandedView;
        TransitionManager.beginDelayedTransition(vg);

        int marginLeft = getResources().getDimensionPixelSize(R.dimen.margin_orig);

        for (int i = arrayImageButton.length - 1; i >= 0; i--) {
            ImageButton ib = arrayImageButton[i];
            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) ib.getLayoutParams();
            rlp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            rlp.setMargins(marginLeft, 0, 0, 0);
            ib.setLayoutParams(rlp);
        }

    }

    private void resetPosition(int x_cord_now) {
        if (x_cord_now <= szWindow.x / 2) {
            isLeft = true;
            moveToLeft(x_cord_now);
        } else {
            isLeft = false;
            moveToRight(x_cord_now);
        }
    }

    /*  Method to move the Floating widget view to Left  */
    private void moveToLeft(final int current_x_cord) {
        final int x = szWindow.x - current_x_cord;

        WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) mFloatingWidgetView.getLayoutParams();
        int step = 1;

        while (mParams.x != 0) {
            mParams.x = 0 - (int) current_x_cord * current_x_cord * step;
            mWindowManager.updateViewLayout(mFloatingWidgetView, mParams);
            step++;
            if (mParams.x < 0) {
                mParams.x = 0;
                mWindowManager.updateViewLayout(mFloatingWidgetView, mParams);
            }
        }
    }

    /*  Method to move the Floating widget view to Right  */
    private void moveToRight(final int current_x_cord) {

        WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) mFloatingWidgetView.getLayoutParams();
        int step = 1;

        while (mParams.x != szWindow.x - mFloatingWidgetView.getWidth()) {
            mParams.x = (int) (szWindow.x + (current_x_cord * current_x_cord * step) - mFloatingWidgetView.getWidth());
            mWindowManager.updateViewLayout(mFloatingWidgetView, mParams);
            step++;
            if (mParams.x > szWindow.x - mFloatingWidgetView.getWidth()) {
                mParams.x = szWindow.x - mFloatingWidgetView.getWidth();
                mWindowManager.updateViewLayout(mFloatingWidgetView, mParams);
            }
        }
    }

    /*  Implement Touch Listener to Floating Widget Root View  */
    private void implementTouchListenerToFloatingWidgetView() {
        //Drag and move floating view using user's touch action.
        mFloatingWidgetView.findViewById(R.id.root_container)
                .setOnTouchListener(
                        new View.OnTouchListener() {
                            long time_start = 0, time_end = 0;

                            @Override
                            public boolean onTouch(View v, MotionEvent event) {

                                //Get Floating widget view params
                                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) mFloatingWidgetView.getLayoutParams();

                                //get the touch location coordinates
                                int x_cord = (int) event.getRawX();
                                int y_cord = (int) event.getRawY();

                                int x_cord_Destination, y_cord_Destination;

                                switch (event.getAction()) {
                                    case MotionEvent.ACTION_DOWN:
                                        time_start = System.currentTimeMillis();

                                        x_init_cord = x_cord;
                                        y_init_cord = y_cord;

                                        //remember the initial position.
                                        x_init_margin = layoutParams.x;
                                        y_init_margin = layoutParams.y;

                                        //resize floatingwidget on click, make it smaller
                                        if (isViewCollapsed()) {
                                            layoutParams.width = getResources().getDimensionPixelSize(R.dimen.small_dimen);
                                            layoutParams.height = getResources().getDimensionPixelSize(R.dimen.small_dimen);

                                            mWindowManager.updateViewLayout(mFloatingWidgetView, layoutParams);
                                        }


                                        return true;
                                    case MotionEvent.ACTION_UP:
                                        //Get the difference between initial coordinate and current coordinate
                                        int x_diff = x_cord - x_init_cord;
                                        int y_diff = y_cord - y_init_cord;

                                        //The check for x_diff <5 && y_diff< 5 because sometime elements moves a little while clicking.
                                        //So that is click event.
                                        Log.e("FloatingWidget", "Motion Event up Callback");
                                        if (Math.abs(x_diff) < 5 && Math.abs(y_diff) < 5) {
                                            time_end = System.currentTimeMillis();

                                            //Also check the difference between start time and end time should be less than 300ms
                                            if ((time_end - time_start) < 300)
                                                onFloatingWidgetClick();

                                        }

                                        y_cord_Destination = y_init_margin + y_diff;

                                        int barHeight = getStatusBarHeight();
                                        if (y_cord_Destination < 0) {
                                            y_cord_Destination = 0;
                                        } else if (y_cord_Destination + (mFloatingWidgetView.getHeight() + barHeight) > szWindow.y) {
                                            y_cord_Destination = szWindow.y - (mFloatingWidgetView.getHeight() + barHeight);
                                        }

                                        layoutParams.y = y_cord_Destination;

                                        //reset position if user drags the floating view
                                        resetPosition(x_cord);
                                        //resize floatingwidget on click, make it bigger
                                        if (isViewCollapsed()) {
                                            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                                            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

                                            mWindowManager.updateViewLayout(mFloatingWidgetView, layoutParams);
                                        }

                                        return true;
                                    case MotionEvent.ACTION_MOVE:
                                        int x_diff_move = x_cord - x_init_cord;
                                        int y_diff_move = y_cord - y_init_cord;

                                        x_cord_Destination = x_init_margin + x_diff_move;
                                        y_cord_Destination = y_init_margin + y_diff_move;

                                        layoutParams.x = x_cord_Destination;
                                        layoutParams.y = y_cord_Destination;

                                        //Update the layout with new X & Y coordinate
                                        mWindowManager.updateViewLayout(mFloatingWidgetView, layoutParams);
                                        return true;
                                }
                                return false;
                            }
                        });
    }

    private void implementClickListeners() {
        mFloatingWidgetView.findViewById(R.id.floating_widget_image_view).setOnClickListener(this);
        for (ImageButton ib : arrayImageButton) {
            ib.setOnClickListener(this);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        //init WindowManager
        mShareService = ShareService.getInstance();
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        getWindowManagerDefaultDisplay();

        //Init LayoutInflater
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        //addRemoveView(inflater);
        addFloatingWidgetView(inflater);
        implementClickListeners();
        implementTouchListenerToFloatingWidgetView();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.floating_widget_image_view:
                hideButtons();
                moveToLeft(0);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        WindowManager.LayoutParams params = (WindowManager.LayoutParams) mFloatingWidgetView.getLayoutParams();
                        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
                        mFloatingWidgetView.setLayoutParams(params);
                        mWindowManager.updateViewLayout(mFloatingWidgetView, params);
                        collapsedView.setVisibility(View.VISIBLE);
                        expandedView.setVisibility(View.GONE);
                    }
                }, 400);
                break;
            case R.id.circleIv1:
                stopSelf();
                break;
            case R.id.circleIv2:
                //TODO: to do send notification to clients the mirroring is paused
                if (!mShareService.onPauseScreenMirroringServer()) {
                    mShareService.pauseScreenMirroringServer();
                } else {
                    mShareService.resumeScreenMirroringServer();
                }
                break;
            case R.id.circleIv3:
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
                break;
            case R.id.circleIv4:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mShareService.sendCommandToAllClientsConnected(ShareService.FACE_ANALYSIS_ON);
                    }
                }).start();
                break;
            case R.id.circleIv5:
                break;
            case R.id.circleIv6:
                break;
        }
    }

    /*  Detect if the floating view is collapsed or expanded */
    private boolean isViewCollapsed() {
        return mFloatingWidgetView == null || mFloatingWidgetView.findViewById(R.id.collapse_view).getVisibility() == View.VISIBLE;
    }


    /*  return status bar height on basis of device display metrics  */
    private int getStatusBarHeight() {
        return (int) Math.ceil(25 * getApplicationContext().getResources().getDisplayMetrics().density);
    }


    /*  Update Floating Widget view coordinates on Configuration change  */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        getWindowManagerDefaultDisplay();

        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) mFloatingWidgetView.getLayoutParams();

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {


            if (layoutParams.y + (mFloatingWidgetView.getHeight() + getStatusBarHeight()) > szWindow.y) {
                layoutParams.y = szWindow.y - (mFloatingWidgetView.getHeight() + getStatusBarHeight());
                mWindowManager.updateViewLayout(mFloatingWidgetView, layoutParams);
            }

            if (layoutParams.x != 0 && layoutParams.x < szWindow.x) {
                resetPosition(szWindow.x);
            }

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

            if (layoutParams.x > szWindow.x) {
                resetPosition(szWindow.x);
            }

        }

    }

    /*  on Floating widget click show expanded view  */
    private void onFloatingWidgetClick() {
        if (isViewCollapsed()) {
            //When user clicks on the image view of the collapsed layout,
            //visibility of the collapsed layout will be changed to "View.GONE"
            //and expanded view will become visible.
            collapsedView.setVisibility(View.GONE);
            expandedView.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showButtons();
                }
            }, 10);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        /*  on destroy remove both view from window manager */

        if (mFloatingWidgetView != null)
            mWindowManager.removeView(mFloatingWidgetView);

//        if (removeFloatingWidgetView != null)
//            mWindowManager.removeView(removeFloatingWidgetView);

    }


}
