package com.example.android.presentor.help;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.android.presentor.R;

/**
 * Created by villa on 16/03/2018.
 */

public class SliderAdapterWidgetButtons extends PagerAdapter{

    Context context;
    LayoutInflater layoutInflater;

    public SliderAdapterWidgetButtons(Context context){
        this.context = context;
    }

    public  int[] slide_images = {

            R.drawable.screenshare,
            R.drawable.home_automation,
            R.drawable.notification
    };

    public String[] slide_headings = {
            "Screen Mirroring",
            "Home Automation",
            "Notification"
    };

    public String [] slide_descs = {
            "Display your android phone's screen display on other device’s screen wirelessly in real time",
            "Control your electrical appliances with your android smartphone",
            "Notify your audience with face analysis"
    };


    @Override
    public int getCount() {

        return slide_headings.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == (RelativeLayout) object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.slide_layout, container, false);

        ImageView slideImageView = (ImageView) view.findViewById(R.id.slide_image);
        TextView slideHeading = (TextView) view.findViewById(R.id.slide_heading);
        TextView slideDescription = (TextView) view.findViewById(R.id.slide_desc);
        slideImageView.setImageResource(slide_images[position]);
        slideHeading.setText(slide_headings[position]);
        slideDescription.setText(slide_descs[position]);

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        container.removeView((RelativeLayout)object);
    }
}
