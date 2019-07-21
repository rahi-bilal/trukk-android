package com.trukk;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.trukk.adapters.HistoryRecyclerViewAdapter;
import com.trukk.adapters.ViewBidsRecyclerViewAdapter;
import com.trukk.fragments.BaseFragment;
import com.trukk.models.Driver;
import com.trukk.models.Pivot;
import com.trukk.models.Shipment;
import com.trukk.volleyrequest.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ViewBidsActivity extends BaseActivity implements View.OnClickListener {

    private RecyclerView rvViewBids;
    private ViewBidsRecyclerViewAdapter adapter;
    private ImageButton btnBack;
    private SwipeRefreshLayout srl_view_bids;

    private static boolean isRefresh = false;
    private String shipmentId;
    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_view_bids);

        this.context = this;

        shipmentId = getIntent().getStringExtra("shipment_id");



        findViewsById();

        setOnClickListeners();

        srl_view_bids.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isRefresh = true;
                getBids(isRefresh);
            }
        });

        getBids(isRefresh);

    }

    private void findViewsById() {
        rvViewBids = findViewById(R.id.rv_view_bids);
        btnBack = findViewById(R.id.btn_back_view_bids);
        srl_view_bids = findViewById(R.id.srl_view_bids);
    }

    private void setOnClickListeners(){
        btnBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_back_view_bids:   this.onBackPressed();
                                            break;

        }
    }

    public void getBids(final boolean isRefresh) {
        //check internet connection
        if (!connectionAvailable()) {
            showSnackbar();
            srl_view_bids.setRefreshing(false);
            return;
        }

        final StringRequest historyRequest = new StringRequest(Request.Method.POST, BaseActivity.API + "/user/show_driver_requests",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("ViewBidsActivity", response);
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            if (status.equals("success")) {
                                JSONArray dataArrary = jsonResponse.getJSONArray("data");
                                Log.d("Data Array", dataArrary.toString());
                                if (dataArrary.length() != 0) {
                                    JSONObject jsonObject = dataArrary.getJSONObject(0);
                                    JSONArray driverArray = jsonObject.getJSONArray("driver_interest");
                                    Log.d("DriverArray", driverArray.toString());
                                    if (driverArray.length() != 0) {
                                        ArrayList<Driver> drivers = new ArrayList<>();
                                        for (int i=0; i<driverArray.length(); i++) {
                                            Log.d("For Loop:", i+"th");
                                            Driver driver = new Driver();
                                            Pivot pivot = new Pivot();
                                            JSONObject driverJson = driverArray.getJSONObject(i);
                                            driver.setDriverId(driverJson.getInt("id"));
                                            driver.setFirst_name(driverJson.getString("first_name"));
                                            driver.setLast_name(driverJson.getString("last_name"));
                                            driver.setEmail(driverJson.getString("email"));
                                            driver.setVehicle_type_id(driverJson.getInt("vehicle_type_id"));
                                            //Todo: get admin id also
                                            driver.setVehical_document(driverJson.getString("vehical_document"));
                                            driver.setIdcard(driverJson.getString("idcard"));
                                            driver.setIdcard(driverJson.getString("idcard"));
                                            driver.setDriving_licence(driverJson.getString("driving_licence"));
                                            driver.setDriver_image(driverJson.getString("driver_image"));
                                            JSONObject pivotJson = driverJson.getJSONObject("pivot");
                                            pivot.setShipment_id(pivotJson.getInt("shipment_id"));
                                            pivot.setDriver_id(pivotJson.getInt("driver_id"));
                                            pivot.setBid_amount(pivotJson.getString("bid_amount"));
                                            pivot.setStatus(pivotJson.getInt("status"));
                                            pivot.setDescription(pivotJson.getString("description"));
                                            pivot.setCreated_at(pivotJson.getString("created_at"));
                                            pivot.setShipment_amount(jsonObject.getString("amount"));
                                            driver.setPivot(pivot);
                                            drivers.add(driver);
                                            Log.d("Driver["+i+"]", drivers.get(i).getPivot().getCreated_at());
                                        }
                                        Log.d("Size", drivers.size()+"");
                                        //Set RecyclerView
                                        if (!isRefresh) {
                                            rvViewBids.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                                            adapter = new ViewBidsRecyclerViewAdapter(context, drivers);
                                            Log.d("ViewBidsActivity", "Setting Adapter");
                                            rvViewBids.setAdapter(adapter);
                                        } else {
                                            ViewBidsActivity.isRefresh = false;
                                            adapter.swapData(drivers);
                                        }

                                    } else {
                                        //show no bids message
                                        findViewById(R.id.lyt_no_bids).setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            Log.e("ViewBidsActivity", e.getLocalizedMessage(), e);
                        }
                        srl_view_bids.setRefreshing(false);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Current ", error.getMessage(), error);
                        srl_view_bids.setRefreshing(false);
                    }
                }){

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("shipment_id", shipmentId);
                return params;
            }

            @Override
            public Map<String, String> getHeaders()  {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization","Bearer "+  BaseActivity.getSharedPref(getBaseContext(), BaseActivity.TOKEN));
                return headers;

            }
        };

        srl_view_bids.setRefreshing(true);

        VolleySingleton.getInstance(this).addToRequestQueue(historyRequest);
    }
}

















