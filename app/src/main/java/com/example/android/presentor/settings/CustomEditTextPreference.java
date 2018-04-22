package com.example.android.presentor.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;

public class CustomEditTextPreference extends EditTextPreference {

    public CustomEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomEditTextPreference(Context context) {
        super(context);
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        final AlertDialog dialog = (AlertDialog) getDialog();
        final View positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        getEditText().setError(null);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPositiveButtonClick(v);
            }
        });

    }

    private void onPositiveButtonClick(View v) {
        boolean hasError = parseInteger(getEditText().getText().toString()) < 1 ||
                parseInteger(getEditText().getText().toString()) > 100;

        if (hasError) {
            //somethings wrong
            getEditText().setError("Value must be an integer from 1 - 100.");
            return;
        }
        String value = Integer.toString(parseInteger(getEditText().getText().toString()));
        getEditText().setText(value);
        getEditText().setError(null);
        onClick(getDialog(), DialogInterface.BUTTON_POSITIVE);
        getDialog().dismiss();
    }

    private int parseInteger(String text) {
        int val = -1;
        try {
            val = Integer.parseInt(text);
        } catch (Exception e) {
        }
        return val;
    }
}
