package com.example.android.presentor.settings;

import android.content.Context;
import android.util.AttributeSet;

import com.example.android.presentor.utils.Utility;

public class ValidatingEditTextPreference extends CustomEditTextPreference {
    public ValidatingEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ValidatingEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ValidatingEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ValidatingEditTextPreference(Context context) {
        super(context);
    }

    @Override
    protected int validateText(String text) {
        int value = 1;
        setErrorMessage("Name must  not contain any special character");
        setBool(false);
        boolean hasSpecialCharacter = Utility.checkForSpecialCharacter(text);
        if(hasSpecialCharacter) value = -1;
        return  value;
    }
}
