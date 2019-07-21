package com.trukk.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.trukk.BaseActivity;
import com.trukk.R;
import com.trukk.adapters.HistoryRecyclerViewAdapter;
import com.trukk.models.Shipment;
import com.trukk.models.Vehicle;
import com.trukk.volleyrequest.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CurrentHistoryFragment extends BaseFragment {
    private static final String LOG_TAG = "CurrentHistoryFragment";

    private String arg1, arg2;

    private BaseFragment baseFragment;
    private ArrayList<Vehicle> vehicleList;
    private Fragment fragment;

    View view;
    RecyclerView rvHistory;
    SwipeRefreshLayout refreshLayout;
    HistoryRecyclerViewAdapter historyAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            arg1 = getArguments().getString("param1");
            arg2 = getArguments().getString("param2");
        }

        baseFragment = ((BaseFragment) getParentFragment());
        vehicleList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.history_recycler_view, container, false);

        findViewsById();

        setRefreshListeners();

        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            loadVehicleList();
            getAllShipments(false);
        }else {
            Toast.makeText(getContext(), "Connection unavailable", Toast.LENGTH_SHORT).show();
        }

        this.fragment = this;

        return view;
    }

    private void findViewsById() {
        refreshLayout = view.findViewById(R.id.refresh_layout);
        rvHistory = view.findViewById(R.id.rv_history);
    }

    private void setRefreshListeners() {
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getAllShipments(true);
            }
        });
    }

    public static CurrentHistoryFragment newInstance(String param1, String param2) {
        CurrentHistoryFragment fragment = new CurrentHistoryFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    private void loadVehicleList() {
        //checking internet connection
        if (!connectionAvailable()) {
            showSnackbar();
            refreshLayout.setRefreshing(false);
            return;
        }

        StringRequest vehicleListRequest = new StringRequest(Request.Method.GET, BaseActivity.API + "/vehicle/list",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        refreshLayout.setRefreshing(false);
                        JSONObject jsonResponse = null;
                        String status = null;
                        Log.d("CurrentHistoryFragment", response);
                        try {
                            jsonResponse = new JSONObject(response);
                            status = jsonResponse.getString("status");
                            if (status.equals("success")) {
                                JSONArray data = jsonResponse.getJSONArray("data");
                                if (data.length() != 0) {

                                    for (int i=0; i<data.length(); i++) {
                                        Vehicle vehicle = new Vehicle();
                                        JSONObject vehicleJson = data.getJSONObject(i);
                                        vehicle.setId(vehicleJson.getInt("id"));
                                        vehicle.setVehicle_name(vehicleJson.getString("vehicle_name"));
                                        vehicleList.add(vehicle);
                                    }
                                    Log.d(LOG_TAG, vehicleList.toString());
                                }
                            }
                        } catch (JSONException e) {
                            Log.e(LOG_TAG, e.getMessage(), e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        refreshLayout.setRefreshing(false);
                        Log.e(LOG_TAG, error.getMessage(), error);
                    }
                });
        refreshLayout.setRefreshing(true);
        VolleySingleton.getInstance(getContext()).addToRequestQueue(vehicleListRequest);
    }

    public void getAllShipments(final boolean isRefresh) {
        //checking internet connection
        if (!connectionAvailable()) {
            showSnackbar();
            refreshLayout.setRefreshing(false);
            return;
        }

        StringRequest historyRequest = new StringRequest(Request.Method.POST, BaseActivity.API + "/user/show_all_shipment",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            Log.d(LOG_TAG, response);
                            if (status.equals("success")){
                                JSONArray data = jsonResponse.getJSONArray("data");
                                if (data.length() != 0){
                                    ArrayList<Shipment> shipments = new ArrayList<>();
                                    for (int i=0; i<data.length(); i++) {
                                        Shipment shipment = new Shipment();
                                        JSONObject shipmentData = data.getJSONObject(i);
                                        shipment.setShipment_id(shipmentData.getInt("id"));

                                        if (shipmentData.getString("vehicle_type_id") != "null") {
                                            shipment.setVehicle_type_id(shipmentData.getInt("vehicle_type_id"));
                                        }

                                        shipment.setDrop_off_lat(shipmentData.getString("drop_off_lat"));
                                        shipment.setDrop_off_long(shipmentData.getString("drop_off_long"));
                                        shipment.setShipment_type(shipmentData.getString("shipment_type"));
                                        shipment.setDescription(shipmentData.getString("description"));
                                        shipment.setPackaging(shipmentData.getString("packaging"));
                                        shipment.setPackaging_dimensions(shipmentData.getString("packaging_dimensions"));
                                        shipment.setPricing_type(shipmentData.getString("pricing_type"));

                                        //setting drop_off_address
                                        String addressString = shipmentData.getString("address");
                                        if (!addressString.equals("null")) {
                                            JSONObject address = shipmentData.getJSONObject("address");
                                            shipment.setDrop_off_address(address.getString("drop_off_address"));
                                        }

                                        //setting driver name and phone number
                                        JSONArray shippableDriverArray = shipmentData.optJSONArray("shippable_driver");

                                        if (shippableDriverArray.length() != 0) {
                                                JSONObject driver = shippableDriverArray.getJSONObject(0);
                                                shipment.setDriverName(driver.getString("first_name") + " " + driver.getString("last_name"));
                                                shipment.setDriverPhone(driver.getString("mobile"));
                                                Log.d("Driver Details:", shipment.getDriverName() + shipment.getDriverPhone());


                                        }

                                        JSONArray shipmentStates = shipmentData.getJSONArray("shipment_states");
                                        if (shipmentStates.length() != 0) {
                                            //update status and driver name
                                            JSONObject latestState = shipmentStates.getJSONObject((shipmentStates.length()-1));
                                            shipment.setStatus(latestState.getString("status_update"));
                                        } else {
                                            shipment.setStatus("Open");
                                        }

                                        if (arg1.equals("current")){
                                            if (!shipment.getStatus().equals("delivered")){
                                                shipments.add(shipment);
                                            }
                                        } else{
                                            if (shipment.getStatus().equals("delivered")){
                                                shipments.add(shipment);
                                            }
                                        }
                                    }
                                    //Setting up history recycler view
                                    refreshLayout.setRefreshing(false);
                                    if (!isRefresh){
                                        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
                                        historyAdapter = new HistoryRecyclerViewAdapter(getActivity(), fragment,  shipments, vehicleList);
                                        rvHistory.setAdapter(historyAdapter);
                                    } else {
                                        Log.d(LOG_TAG, "sending swapping details");
                                        historyAdapter.swapData(shipments);
                                    }

                                }else {
                                    //show no shipments yet message
                                }

                            } else {
                                if (status.equals("error")) {

                                    return;
                                }
                                JSONObject errorsObject = jsonResponse.optJSONObject("errors");
                                if(errorsObject!= null){
                                    JSONArray mobileErrorArray = errorsObject.optJSONArray("mobile");
                                    Log.d(LOG_TAG, mobileErrorArray.toString());
                                    String er = mobileErrorArray.getString(0);
                                    if (er!= null){
                                        Log.d(LOG_TAG, er);
                                        showMessageDialog(er);
                                        return;
                                    }
                                }
                                String message = jsonResponse.optString("message");
                                if (message != null){
                                    Log.d( LOG_TAG, message);
                                    showMessageDialog(message);
                                    return;
                                }
                            }
                        }catch (JSONException e){
                            Log.d(LOG_TAG, e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d( LOG_TAG, error.getMessage(), error);
                        baseFragment.hideProgressDialog();
                        showMessageDialog(error.getMessage());
                    }
                }){

            @Override
            public Map<String, String> getHeaders()  {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization","Bearer "+  BaseActivity.getSharedPref(getContext(), BaseActivity.TOKEN));
                return headers;

            }
        };
        refreshLayout.setRefreshing(true);
        VolleySingleton.getInstance(getContext()).addToRequestQueue(historyRequest);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 500) {
            getAllShipments(false);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d("CurrentHistory Fragment", "Permission Result:");
        if (requestCode == REQUEST_PHONE_CALL) {
            if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                historyAdapter.callPhone();
            }
        }

    }
}
