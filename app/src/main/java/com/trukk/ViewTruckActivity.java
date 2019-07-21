package com.trukk;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.trukk.adapters.ViewTruckPagerAdapter;
import com.trukk.volleyrequest.VolleySingleton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ViewTruckActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "ViewTruckActivity";
    private int driver_id;
    private ViewPager view_pager_truck_image;
    private ImageButton img_prev, img_next, btn_back_view_truck;
    private ArrayList<String> imageUrls;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_view_truck);

        //getting arguments
        if(getIntent().getExtras() != null) {
            driver_id = getIntent().getIntExtra("id", 0);
        }

        findViewsById();

        setOnClickListeners();


        getTrucks();
    }

    private void findViewsById() {
        view_pager_truck_image = findViewById(R.id.view_pager_truck_image);
        img_prev = findViewById(R.id.img_prev);
        img_next = findViewById(R.id.img_next);
        btn_back_view_truck = findViewById(R.id.btn_back_view_truck);
    }

    private void setOnClickListeners() {
        img_prev.setOnClickListener(this);
        img_next.setOnClickListener(this);
        btn_back_view_truck.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_prev: {
                if (view_pager_truck_image.getCurrentItem() != 0) {
                    view_pager_truck_image.setCurrentItem((view_pager_truck_image.getCurrentItem()-1));
                }
                break;
            }

            case R.id.img_next: {
                if (view_pager_truck_image.getCurrentItem() != imageUrls.size()-1) {
                    view_pager_truck_image.setCurrentItem((view_pager_truck_image.getCurrentItem()+1));
                }
                break;
            }
            case R.id.btn_back_view_truck:{
                super.onBackPressed();
                break;
            }
        }
    }

    private void getTrucks() {
        //check internet connection
        if (!connectionAvailable()) {
            showSnackbar();
            return;
        }

        String url = String.format("%s/user/account/driver", API);
        Log.d(TAG, url);
        StringRequest driverRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Res: "+response);
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            if (status.equals("success")) {
                                JSONObject jsonData = jsonResponse.getJSONObject("data");
                                JSONArray vehicleImageArray = jsonData.getJSONArray("vehicle_image");
                                if (vehicleImageArray.length()>0){
                                    imageUrls = new ArrayList<>();
                                    for (int i=0; i<vehicleImageArray.length(); i++) {
                                        JSONObject vehice = vehicleImageArray.getJSONObject(i);
                                        imageUrls.add(vehice.getString("image"));
                                    }
                                    Log.d(TAG, Integer.toString(imageUrls.size()));
                                    view_pager_truck_image.setAdapter(new ViewTruckPagerAdapter(getSupportFragmentManager(), imageUrls));
                                }
                            }
                        }catch (JSONException e) {
                            Log.e(TAG, e.getMessage(), e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error: "+error.getMessage(), error);
                    }
                })
        {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("driver_id", Integer.toString(driver_id));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer "+getSharedPref(getApplicationContext(), TOKEN));
                return headers;
            }
        };

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(driverRequest);
    }

}
