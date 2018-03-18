package com.example.android.presentor.floatingwidget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Handler;
import android.transition.TransitionManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.android.presentor.R;

/**
 * Created by Carlo on 07/03/2018.
 */

public class FloatingWidgetView implements View.OnTouchListener {

    private Context mContext;
    private WindowManager mWindowManager;

    private View mFloatingWidgetView, collapsedView, expandedView;
    private Point szWindow = new Point();
    private ImageButton[] arrayImageButton;

    private int x_init_cord;
    private int y_init_cord;
    private int x_init_margin;
    private int y_init_margin;
    private int height;

    private long time_start = 0;

    public ImageButton getImageButton(int position){
        return arrayImageButton[position];
    }

    public FloatingWidgetView(Context context, View.OnClickListener onClickListener) {
        this.mContext = context;
        height = mContext.getResources().getDimensionPixelOffset(R.dimen.height);
        this.mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        create();
        setTouchListener();
        setClickListener(onClickListener);
    }

    /**
     * create the floating widget view
     */
    private void create() {
        getWindowManagerDefaultDisplay();
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        addFloatingWidgetView(inflater);
    }

    private void setTouchListener() {
        mFloatingWidgetView.findViewById(R.id.root_container).setOnTouchListener(this);
    }

    private void setClickListener(View.OnClickListener listener) {
        mFloatingWidgetView.findViewById(R.id.floating_widget_image_view).setOnClickListener(listener);
        for (ImageButton imageButton : arrayImageButton) {
            imageButton.setOnClickListener(listener);
        }
    }


    /***Add the floating widget view to the screen***/
    private void addFloatingWidgetView(LayoutInflater inflater) {
        //Inflate the floating view layout we created
        mFloatingWidgetView = inflater.inflate(R.layout.floating_widget_layout, null);

        //Add the view to the window.

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                height,
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
        //find the buttons of expanded view
        arrayImageButton = new ImageButton[6];
        arrayImageButton[0] = expandedView.findViewById(R.id.circleIv1);    //stop
        arrayImageButton[1] = expandedView.findViewById(R.id.circleIv2);    //play/pause
        arrayImageButton[2] = expandedView.findViewById(R.id.circleIv3);    //pin
        arrayImageButton[3] = expandedView.findViewById(R.id.circleIv4);    //face
        arrayImageButton[4] = expandedView.findViewById(R.id.circleIv5);    //domotics
        arrayImageButton[5] = expandedView.findViewById(R.id.circleIv6);    //screen share
    }

    /***Get the screen resolution of the current device***/
    private void getWindowManagerDefaultDisplay() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int h = displayMetrics.heightPixels;
        int w = displayMetrics.widthPixels;
        szWindow.set(w, h);
    }

    /***Show expanded view with animation***/
    private void showButtons() {
        //to make the width of expanded view match the screen
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) mFloatingWidgetView.getLayoutParams();
        params.width = szWindow.x;
        mFloatingWidgetView.setLayoutParams(params);
        mWindowManager.updateViewLayout(mFloatingWidgetView, params);

        ViewGroup vg = (ViewGroup) expandedView;
        TransitionManager.beginDelayedTransition(vg);

        int subButton = mContext.getResources().getDimensionPixelSize(R.dimen.widget_button_dimen);
        int marginLeft = mContext.getResources().getDimensionPixelSize(R.dimen.margin_to_logo);
        int marginAllowance = mContext.getResources().getDimensionPixelSize(R.dimen.margin_allowance);


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

    /***Hide expanded view with animation***/
    private void hideButtons() {
        ViewGroup vg = (ViewGroup) expandedView;
        TransitionManager.beginDelayedTransition(vg);

        int marginLeft = mContext.getResources().getDimensionPixelSize(R.dimen.margin_orig);

        for (int i = arrayImageButton.length - 1; i >= 0; i--) {
            ImageButton ib = arrayImageButton[i];
            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) ib.getLayoutParams();
            rlp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            rlp.setMargins(marginLeft, 0, 0, 0);
            ib.setLayoutParams(rlp);
        }

    }

    /**
     * Reset the position of floating widget, to the nearest side, either left or right
     *
     * @param x_cord_now current x coordinate of floating widget
     */
    private void resetPosition(int x_cord_now) {
        if (x_cord_now <= szWindow.x / 2) {
            moveToLeft(x_cord_now);
        } else {
            moveToRight(x_cord_now);
        }
    }

    /**
     * Move the floating widgeet to the left, with animation
     *
     * @param current_x_cord current x coordinate of floating widget
     */
    private void moveToLeft(final int current_x_cord) {
        final int x = szWindow.x - current_x_cord;

        WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) mFloatingWidgetView.getLayoutParams();
        int step = 1;

        while (mParams.x != 0) {
            mParams.x = 0 - current_x_cord * current_x_cord * step;
            mWindowManager.updateViewLayout(mFloatingWidgetView, mParams);
            step++;
            if (mParams.x < 0) {
                mParams.x = 0;
                mWindowManager.updateViewLayout(mFloatingWidgetView, mParams);
            }
        }
    }

    /**
     * Move the floating widgeet to the right, with animation
     *
     * @param current_x_cord current x coordinate of floating widget
     */
    private void moveToRight(final int current_x_cord) {

        WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) mFloatingWidgetView.getLayoutParams();
        int step = 1;

        while (mParams.x != szWindow.x - mFloatingWidgetView.getWidth()) {
            mParams.x = (szWindow.x + (current_x_cord * current_x_cord * step) - mFloatingWidgetView.getWidth());
            mWindowManager.updateViewLayout(mFloatingWidgetView, mParams);
            step++;
            if (mParams.x > szWindow.x - mFloatingWidgetView.getWidth()) {
                mParams.x = szWindow.x - mFloatingWidgetView.getWidth();
                mWindowManager.updateViewLayout(mFloatingWidgetView, mParams);
            }
        }
    }

    /**
     * Detect if the floating view is collapsed or expanded
     */
    private boolean isViewCollapsed() {
        return mFloatingWidgetView == null || mFloatingWidgetView.findViewById(R.id.collapse_view).getVisibility() == View.VISIBLE;
    }

    /**
     * Collapsed the floating widget view
     */
    public void onViewCollapsed() {
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
    }


    /**
     * return status bar height on basis of device display metrics
     */
    private int getStatusBarHeight() {
        return (int) Math.ceil(25 * mContext.getResources().getDisplayMetrics().density);
    }

    /**
     * on Floating widget click show expanded view
     * When user clicks on the image view of the collapsed layout,
     * visibility of the collapsed layout will be changed to "View.GONE"
     * and expanded view will become visible.
     */
    private void onFloatingWidgetClick() {
        if (isViewCollapsed()) {
            //When user clicks on the image view of the collapsed layout,
            //visibility of the collapsed layout will be changed to "View.GONE"
            //and expanded view will become visible.
            collapsedView.setVisibility(View.GONE);
            expandedView.setVisibility(View.VISIBLE);

            WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) mFloatingWidgetView.getLayoutParams();
            layoutParams.height = height;
            mWindowManager.updateViewLayout(mFloatingWidgetView, layoutParams);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showButtons();
                }
            }, 10);
        }
    }

    /**
     * @param newConfig Adjust floating widget view depending on the orientation of the phone
     */
    public void handleOrientationChanges(Configuration newConfig) {
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

    /**
     * Remove the floating widget view
     */
    public void removeView() {
        if (mFloatingWidgetView != null) {
            mWindowManager.removeView(mFloatingWidgetView);
        }
    }


    //-------------------------Interface implementation for onTouch and onClick--------------------//

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        view.performClick();
        //Get Floating widget view params
        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) mFloatingWidgetView.getLayoutParams();

        //get the touch location coordinates
        int x_cord = (int) motionEvent.getRawX();
        int y_cord = (int) motionEvent.getRawY();

        int x_cord_Destination, y_cord_Destination;

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                time_start = System.currentTimeMillis();

                x_init_cord = x_cord;
                y_init_cord = y_cord;

                //remember the initial position.
                x_init_margin = layoutParams.x;
                y_init_margin = layoutParams.y;

                //resize floatingwidget on click, make it smaller
                if (isViewCollapsed()) {
                    layoutParams.width = mContext.getResources().getDimensionPixelSize(R.dimen.small_dimen);
                    layoutParams.height = mContext.getResources().getDimensionPixelSize(R.dimen.small_dimen);

                    mWindowManager.updateViewLayout(mFloatingWidgetView, layoutParams);
                }
                return true;
            case MotionEvent.ACTION_UP:
                //Get the difference between initial coordinate and current coordinate
                int x_diff = x_cord - x_init_cord;
                int y_diff = y_cord - y_init_cord;

                //The check for x_diff <5 && y_diff< 5 because sometime elements moves a little while clicking.
                //So that is click event.
                if (Math.abs(x_diff) < 5 && Math.abs(y_diff) < 5) {
                    long time_end = System.currentTimeMillis();

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
                    layoutParams.height = height;

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
}
