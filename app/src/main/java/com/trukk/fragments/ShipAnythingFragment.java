package com.trukk.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.trukk.MapsActivity;
import com.trukk.R;
import com.trukk.interfaces.OnFragmentInteractionListener;

public class ShipAnythingFragment extends BaseFragment implements View.OnClickListener, DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static int ID_SHIPMENT_TYPE;

    private String mParam1;
    private String mParam2;
    private String date = "";

    private View view;
    private Button btnGo;
    private View lytPickupLocation;
    private View lytPickupTime;
    private View lytCargo;
    private View lytPersonal;
    private TextView edtPickupTime;
    private TextView tvPickupLocation;
    private boolean isDateSet = false;

    private double pickLat, pickLng;
    private String pickupLocation;


    public ShipAnythingFragment() {
        // Required empty public constructor
    }

    public static ShipAnythingFragment newInstance(String param1, String param2) {
        ShipAnythingFragment fragment = new ShipAnythingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_ship_anything, container, false);

        findViewsById();

        setOnClickListeners();

        ID_SHIPMENT_TYPE = 0;
        pickLat = 0;
        pickLng = 0;
        pickup_lat = 0;
        pickup_long = 0;
        pickupLocation = null;
        pickup_time = null;
        shipment_type = null;
        pricing_type = "fixed";


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mListener.updateCurrentFragment("ship_anything_fragment");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void findViewsById() {
        btnGo = view.findViewById(R.id.btn_go);
        lytPickupLocation = view.findViewById(R.id.lyt_pickup_location);
        lytPickupTime = view.findViewById(R.id.lyt_pickup_time);
        lytCargo = view.findViewById(R.id.lyt_cargo);
        lytPersonal = view.findViewById(R.id.lyt_personal);
        tvPickupLocation = view.findViewById(R.id.edt_pickup_location);
        edtPickupTime = view.findViewById(R.id.edt_pickup_time);
    }

    private void setOnClickListeners() {
        btnGo.setOnClickListener(this);
        lytPickupLocation.setOnClickListener(this);
        lytPickupTime.setOnClickListener(this);
        lytCargo.setOnClickListener(this);
        lytPersonal.setOnClickListener(this);
    }

    private void onGoButtonPressed() {
        if (pickLat == 0 && pickLng == 0){
            //Show error dialog
            showMessageDialog("Please select pickup location.");
            return;
        }
        if (!isDateSet) {
            //show error dialog
            showMessageDialog("please select pickup time.");
            return;
        }

        if (ID_SHIPMENT_TYPE == 0) {
            //Show error dialog
            showMessageDialog("Please select shipment type.");
            return;
        }


        pickup_lat = pickLat;
        pickup_long = pickLng;
        pickup_time = edtPickupTime.getText().toString();
        pickup_address = pickupLocation;
        if (ID_SHIPMENT_TYPE == R.id.lyt_cargo) {
            shipment_type = getResources().getString(R.string.cargo);
        } else {
            shipment_type = getResources().getString(R.string.personal);
        }

        mListener.shipAnythingGo();
    }

    private void updateShipmentType(int id) {
        if (ID_SHIPMENT_TYPE == id){
            return;
        }
        if (ID_SHIPMENT_TYPE == 0){
            if (id == R.id.lyt_cargo){
                ID_SHIPMENT_TYPE = R.id.lyt_personal;
            } else {
                ID_SHIPMENT_TYPE = R.id.lyt_cargo;
            }
        }

        if (ID_SHIPMENT_TYPE == R.id.lyt_cargo){
            //Un-select current cargo shipment type
            ImageView imgShipmentBg = view.findViewById(R.id.img_cargo_bg);
            ImageView imgShipmentVehice = view.findViewById(R.id.img_cargo_vehicle);
            TextView tvShipment = view.findViewById(R.id.tv_cargo);

            imgShipmentBg.setImageDrawable(getResources().getDrawable(R.drawable.circle_white));
            imgShipmentVehice.setImageDrawable(getResources().getDrawable(R.drawable.cargo_orange));
            tvShipment.setTextColor(getResources().getColor(android.R.color.black));

            //select current shipment type
            imgShipmentBg = view.findViewById(R.id.img_personal_bg);
            imgShipmentVehice = view.findViewById(R.id.img_personal_vehicle);
            tvShipment = view.findViewById(R.id.tv_personal);

            imgShipmentBg.setImageDrawable(getResources().getDrawable(R.drawable.circle_orange));
            imgShipmentVehice.setImageDrawable(getResources().getDrawable(R.drawable.ic_personal_white));
            tvShipment.setTextColor(getResources().getColor(android.R.color.white));

            //update new selected shipment id
            ID_SHIPMENT_TYPE = R.id.lyt_personal;
        }else {
            //Un-select current cargo shipment type
            ImageView imgShipmentBg = view.findViewById(R.id.img_personal_bg);
            ImageView imgShipmentVehice = view.findViewById(R.id.img_personal_vehicle);
            TextView tvShipment = view.findViewById(R.id.tv_personal);

            imgShipmentBg.setImageDrawable(getResources().getDrawable(R.drawable.circle_white));
            imgShipmentVehice.setImageDrawable(getResources().getDrawable(R.drawable.ic_personal_orange));
            tvShipment.setTextColor(getResources().getColor(android.R.color.black));

            //select current shipment type
            imgShipmentBg = view.findViewById(R.id.img_cargo_bg);
            imgShipmentVehice = view.findViewById(R.id.img_cargo_vehicle);
            tvShipment = view.findViewById(R.id.tv_cargo);

            imgShipmentBg.setImageDrawable(getResources().getDrawable(R.drawable.circle_orange));
            imgShipmentVehice.setImageDrawable(getResources().getDrawable(R.drawable.cargo_white));
            tvShipment.setTextColor(getResources().getColor(android.R.color.white));

            //update new selected shipment id
            ID_SHIPMENT_TYPE = R.id.lyt_cargo;
        }
    }

    private void showDatePickerDialog() {
        DialogFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.show(getChildFragmentManager(), "datePicker");
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        month++;
        date = "";
        if(month < 10){
            date += year+ "-0"+ month;
        }else {
            date += year + "-" + month;
        }
        if (dayOfMonth<10){
            date += "-0"+ dayOfMonth;
        } else {
            date += "-" + dayOfMonth;
        }

        DialogFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.show(getChildFragmentManager(), "timePicker");
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (hourOfDay <10) {
            date += " 0" + hourOfDay;
        }else {
            date += " " + hourOfDay;
        }

        if (minute < 10) {
            date += ":0" + minute + ":00";
        }else {
            date += ":" + minute + ":00";
        }

        edtPickupTime.setText(date);
        isDateSet = true;
        date="";
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_go:               onGoButtonPressed();
                                            break;
            case R.id.lyt_pickup_location:  Intent intent=new Intent(getActivity(), MapsActivity.class);
                                            startActivityForResult(intent, PICKUP_LOCATION_REQUEST_CODE);
                                            break;
            case R.id.lyt_pickup_time:      showDatePickerDialog();
                                            break;
            case R.id.lyt_cargo:
            case R.id.lyt_personal:         updateShipmentType(v.getId());
                                            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICKUP_LOCATION_REQUEST_CODE) {
            pickLat = data.getDoubleExtra("lat", 0);
            pickLng = data.getDoubleExtra("lng", 0);
            pickupLocation = data.getStringExtra("location");
            if (pickLat != 0) {
                if (pickupLocation != null) {
                    tvPickupLocation.setText(pickupLocation);
                }

            }
        }
    }
}
