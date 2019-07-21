package com.trukk.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.View;

import com.trukk.MainActivity;
import com.trukk.R;
import com.trukk.ViewBidsActivity;
import com.trukk.interfaces.OnFragmentInteractionListener;
import com.trukk.utils.MessageDialog;

public class BaseFragment extends Fragment {

    public final static int PICKUP_LOCATION_REQUEST_CODE = 101;
    public final static int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    public final static int REQUEST_IMAGE_CAPTURE = 300;
    public final static int REQUEST_IMAGE_GALLERY = 400;
    public final static int REQUEST_PHONE_CALL = 800;

    protected OnFragmentInteractionListener mListener;

    protected Context context;

    public static double pickup_lat = 0, pickup_long = 0, drop_off_lat= 0, drop_off_long = 0;
    public static String pickup_address = null;
    public static String drop_off_address = null;
    public static String vehicle_type_id = null;
    public static String pickup_time = null;
    public static String shipment_type = null;
    public static String packaging = null;
    public static String labour_handling = null;
    public static String unload_handling = null;
    public static String pricing_type = null;
    public static String order_type = null;

    ProgressDialog progressDialog;
    MessageDialog messageDialog;

    public BaseFragment() {

    }

    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
            this.context = context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        progressDialog = new ProgressDialog(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public boolean connectionAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public  void showProgressDialog(String message, Boolean cancelelable){

        progressDialog.setMessage(message);
        progressDialog.setCancelable(cancelelable);
        progressDialog.show();

    }

    public void hideProgressDialog(){
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }

    public void showMessageDialog(String message) {
        messageDialog = MessageDialog.newInstance(message);
        messageDialog.show(getChildFragmentManager(), "message_dialog");
    }

    public void hideMessageDialog() {
        if (messageDialog != null) {
            messageDialog.dismiss();
        }
    }

    public void showSnackbar() {
        Snackbar snackbar = Snackbar.make(((MainActivity)context).findViewById(R.id.drawer_layout), "No internet connection", Snackbar.LENGTH_LONG)
                .setAction("Settings", new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                });
        snackbar.show();
    }

}
