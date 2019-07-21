package com.trukk.adapters;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.trukk.BaseActivity;
import com.trukk.MainActivity;
import com.trukk.R;
import com.trukk.ViewBidsActivity;
import com.trukk.fragments.BaseFragment;
import com.trukk.fragments.CurrentHistoryFragment;
import com.trukk.fragments.HistoryFragment;
import com.trukk.models.Shipment;
import com.trukk.models.Vehicle;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.HistoryViewHolder> {
    private static final String TAG = "HistoryRVAdapter";
    private Context context;
    private ArrayList<Shipment> shipments;
    private ArrayList<Vehicle> vehicles;
    private String currentOrPast;
    private String phoneNumber;
    private Fragment fragment;

    public HistoryRecyclerViewAdapter(Context context, Fragment fragment, ArrayList<Shipment> shipments, ArrayList<Vehicle> vehicles) {
        this.context = context;
        this.shipments = shipments;
        this.vehicles = vehicles;
        this.currentOrPast = currentOrPast;
        this.fragment = fragment;
    }

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_history_vh, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HistoryViewHolder viewHolder, final int i) {

        String status = shipments.get(i).getStatus();
        Log.d("status", status);

        //setting description
        viewHolder.tv_history_description.setText(shipments.get(i).getDescription());

        //setting drop off location
        viewHolder.tv_history_drop_off.setText(shipments.get(i).getDrop_off_address());

        //Setting truck type
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getId() == shipments.get(i).getVehicle_type_id()) {
                viewHolder.tv_history_truck_type.setText(vehicle.getVehicle_name());
            }
        }

        //Setting cartoon weight
        JSONObject dimensions;
        try {
            dimensions = new JSONObject(shipments.get(i).getPackaging_dimensions());
            String weight = dimensions.optString("weight", "-");
            viewHolder.tv_history_cartoon_weight.setText(weight);
        } catch (JSONException e) {
            Log.e("HistoryRVAdapter", e.getLocalizedMessage(), e);
        }

        //Setting cartoon qty
        try {
            dimensions = new JSONObject(shipments.get(i).getPackaging_dimensions());
            String qty = dimensions.optString("qty", "-");
            viewHolder.tv_history_qty.setText(qty);

        } catch (JSONException e) {
            Log.e("HistoryRVAdapter", e.getLocalizedMessage(), e);
        }

        //Setting pricing
        viewHolder.tv_history_price.setText(shipments.get(i).getPricing_type());

        //Setting packaging
        viewHolder.tv_history_packaging.setText(shipments.get(i).getPackaging());

        //Setting Driver name and phone
        if (shipments.get(i).getDriverName() != null){
            viewHolder.lyt_linear3.setVisibility(View.VISIBLE);
            viewHolder.tv_history_driver.setText(shipments.get(i).getDriverName());
            viewHolder.img_history_call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startCalling(i);
                }
            });
        } else {
            viewHolder.lyt_linear3.setVisibility(View.GONE);
        }

        if(status.equals("delivered")) {
            viewHolder.lyt_linear3.setVisibility(View.GONE);
        }


        //setting status
        if (status.equals("order_placed")) {
            viewHolder.tv_history_status.setText("Order Placed");
        } else if (status.equals("pickup_inprogress")) {
            viewHolder.tv_history_status.setText("Pickup in progress");
        }else if (status.equals("packaging_inprogress")) {
            viewHolder.tv_history_status.setText("Packaging in progress");
        }else if (status.equals("in_transit")) {
            viewHolder.tv_history_status.setText("In transit");
        }else if (status.equals("delivered")) {
            viewHolder.tv_history_status.setText("Delivered");
        }else if (status.equals("undelivered")) {
            viewHolder.tv_history_status.setText("Un-delivered");
        }else if (status.equals("undelivered")){
            viewHolder.tv_history_status.setText("Un-delivered");
        } else {
            viewHolder.tv_history_status.setText(status);
        }


        //hide view bids text view if order is already placed
        if (!shipments.get(i).getStatus().equals("Open")){
            viewHolder.tv_history_view_bids.setVisibility(View.GONE);
        } else {
            viewHolder.tv_history_view_bids.setVisibility(View.VISIBLE);
        }

        //Setting shipment id as Tag for future reference
        viewHolder.tv_history_view_bids.setTag(shipments.get(i).getShipment_id());
        //Set onclick listener
        viewHolder.tv_history_view_bids.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewBidsActivity.class);
                intent.putExtra("shipment_id", v.getTag().toString());
                fragment.startActivityForResult(intent, 555);
            }
        });

    }

    public void swapData(ArrayList<Shipment> newShipments) {
        shipments = newShipments;
        Log.d(TAG, "notifying data-set change");
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return shipments.size();
    }


    public void startCalling(int position) {
        phoneNumber = shipments.get(position).getDriverPhone();
        if ( ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED) {
         fragment.requestPermissions( new String[]{Manifest.permission.CALL_PHONE}, BaseFragment.REQUEST_PHONE_CALL);

        } else {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+ phoneNumber));
            context.startActivity(intent);
        }
    }

    public void callPhone() {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+ phoneNumber));
        context.startActivity(intent);
    }

    public class HistoryViewHolder extends RecyclerView.ViewHolder{

        TextView tv_history_description;
        TextView tv_history_drop_off;
        TextView tv_history_truck_type;
        TextView tv_history_cartoon_weight;
        TextView tv_history_qty;
        TextView tv_history_view_bids;
        TextView tv_history_price;
        TextView tv_history_packaging;
        TextView tv_history_status;
        TextView tv_history_driver;
        ImageView img_history_call;
        LinearLayout lyt_linear3;

        public HistoryViewHolder(View view){
            super(view);
            tv_history_description = view.findViewById(R.id.tv_history_description);
            tv_history_drop_off = view.findViewById(R.id.tv_history_drop_off);
            tv_history_truck_type = view.findViewById(R.id.tv_history_truck_type);
            tv_history_cartoon_weight = view.findViewById(R.id.tv_history_cartoon_weight);
            tv_history_qty = view.findViewById(R.id.tv_history_qty);
            tv_history_view_bids = view.findViewById(R.id.tv_history_view_bids);
            tv_history_price = view.findViewById(R.id.tv_history_price);
            tv_history_packaging = view.findViewById(R.id.tv_history_packaging);
            tv_history_status = view.findViewById(R.id.tv_history_status);
            tv_history_driver = view.findViewById(R.id.tv_history_driver);
            img_history_call = view.findViewById(R.id.img_history_call);
            lyt_linear3 = view.findViewById(R.id.lyt_linear3);
        }
    }
}


