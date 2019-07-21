package com.trukk.fragments;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        TimePickerDialog dialog = new TimePickerDialog(getActivity(), (TimePickerDialog.OnTimeSetListener) getParentFragment(), hour, minute, false);
        dialog.setCancelable(false);
        return dialog;

    }
}
