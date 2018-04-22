package com.example.android.presentor.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;

public class CustomEditTextPreference extends EditTextPreference {

    private String errorMessage;
    private boolean bool;

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
        boolean hasError = validateText(getEditText().getText().toString()) < 1 ||
                validateText(getEditText().getText().toString()) > 100;

        if (hasError) {
            //somethings wrong
            getEditText().setError(errorMessage);
            return;
        }
        if (bool) {
            String value = Integer.toString(validateText(getEditText().getText().toString()));
            getEditText().setText(value);
        }
        getEditText().setError(null);
        onClick(getDialog(), DialogInterface.BUTTON_POSITIVE);
        getDialog().dismiss();
    }


    protected void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    protected void setBool(boolean b) {
        this.bool = b;
    }

    protected int validateText(String text) {
        setErrorMessage("Value must be an integer from 1 - 100.");
        setBool(true);
        int val = -1;
        try {
            val = Integer.parseInt(text);
        } catch (Exception e) {
        }
        return val;
    }
}
