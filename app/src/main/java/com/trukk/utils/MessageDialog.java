package com.trukk.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.trukk.BaseActivity;

public class MessageDialog extends DialogFragment{

    private String message;

    private static final String MESSAGE = "message";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Dialog dialog = builder.setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            message = getArguments().getString(MESSAGE);
        }
    }

    public static MessageDialog newInstance(String message) {
       MessageDialog messageDialog = new MessageDialog();
       Bundle args = new Bundle();
       args.putString(MESSAGE, message);
       messageDialog.setArguments(args);
       return messageDialog;
    }
}
