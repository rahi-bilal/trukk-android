package com.trukk;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.trukk.utils.MessageDialog;

import java.util.ArrayList;

/**
 * Created by Developer on 1/23/18.
 */

public class BaseActivity extends AppCompatActivity {

    public final static String PREFS_NAME              = "vabby";
    public final static String USER_ID                 = "uid";
    public final static String USER_FNAME              = "fname";
    public final static String USER_LNAME              = "lname";
    public final static String USER_EMAIL              = "email";
    public final static String USER_MOBILE             = "mobile";
    public final static String USER_PROVIDER           = "provider";
    public final static String USER_IMAGE              = "image";
    public final static String USER_TYPE               = "type";
    public final static String ACCOUNT_STATUS          = "status";
    public final static String HOME_RES                = "home_res";
    public final static String RADIUS                  = "RADIUS";
    public final static String NOTIFICATION            = "NOTIFICATION";
    public final static String TOKEN                   = "token";
    public final static String IS_LOGIN                = "IS_LOGIN";

    public final static String FROM_REGISTER          = "from_register";

    public final static String API ="http://dev07.codebuzzers.com/trukk/api";



    ProgressDialog progressDialog;
    MessageDialog messageDialog;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES, WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES);

        progressDialog=new ProgressDialog(this);
    }

    public  void showProgressdialog(String message, Boolean cancelelable){

        progressDialog.setMessage(message);
        progressDialog.setCancelable(cancelelable);
        progressDialog.show();

    }

    public void hideProgressdialog(){
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }

    public void showMessageDialog(String message) {
        messageDialog = MessageDialog.newInstance(message);
        messageDialog.show(getSupportFragmentManager(), "message_dialog");
    }

    public void hideMessageDialog() {
        if (messageDialog != null) {
            messageDialog.dismiss();
        }
    }

    public String getDeviceId() {
        return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public boolean connectionAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void showSnackbar() {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.lyt_activity_container), "No internet connection", Snackbar.LENGTH_LONG)
                .setAction("Settings", new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                });
        snackbar.show();
    }

    public static String getSharedPref(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(key, "");
    }

    public static boolean getSharedPrefBoolean(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(key, false);
    }

    public static int getSharedPrefInt(Context context, String key){
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(key, 0);
    }

    public static void setSharedPref(Context context, String key, String value){
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(key,value).apply();
    }

    public static void setSharedPrefInt(Context context, String key, int value){
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(key,value).apply();
    }

    public static void setSharedPrefDouble(Context context, String key, float value){
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putFloat(key,value).apply();
    }

    public static void setSharedPrefBoolean(Context context, String key, boolean value){
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(key,value).apply();
    }

    public static void resetSharedPref(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(USER_ID, null).apply();
        prefs.edit().putString(USER_FNAME, null).apply();
        prefs.edit().putString(USER_LNAME, null).apply();
        prefs.edit().putString(USER_EMAIL, null).apply();
        prefs.edit().putString(USER_MOBILE, null).apply();
        prefs.edit().putString(USER_PROVIDER, null).apply();
        prefs.edit().putString(USER_IMAGE, null).apply();
        prefs.edit().putString(USER_TYPE, null).apply();
        prefs.edit().putString(ACCOUNT_STATUS, null).apply();
        prefs.edit().putString(TOKEN, null).apply();
        prefs.edit().putBoolean(IS_LOGIN, false).apply();
    }


    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


}

