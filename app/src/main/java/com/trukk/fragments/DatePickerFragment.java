package com.trukk.fragments;

import android.app.Dialog;
import android.os.Bundle;

import android.support.v4.app.DialogFragment;
import android.app.DatePickerDialog;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog( Bundle savedInstanceState) {

        //Getting today's date
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        //Creating new instance of DatePickerDialog and return
        DatePickerDialog dialog =  new DatePickerDialog(getActivity(),(DatePickerDialog.OnDateSetListener) getParentFragment() , year, month, day);
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        return dialog;
    }

}
