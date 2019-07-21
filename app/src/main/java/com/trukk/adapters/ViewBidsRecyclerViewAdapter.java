package com.trukk.adapters;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.trukk.BaseActivity;
import com.trukk.MainActivity;
import com.trukk.R;
import com.trukk.ViewBidsActivity;
import com.trukk.ViewTruckActivity;
import com.trukk.models.Driver;
import com.trukk.models.Pivot;
import com.trukk.volleyrequest.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ViewBidsRecyclerViewAdapter extends RecyclerView.Adapter<ViewBidsRecyclerViewAdapter.ViewBidsViewHolder> {
    private static final String TAG = "ViewBidsRVAdapter";
    private ArrayList<Driver> drivers;
    private Context context;

    public ViewBidsRecyclerViewAdapter(Context context, ArrayList<Driver> drivers) {
        this.drivers = drivers;
        this.context = context;
    }

    @Override
    public ViewBidsViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_view_bids_vh, parent, false);
        return new ViewBidsViewHolder(view);

    }

    @Override
    public void onBindViewHolder(ViewBidsViewHolder viewHolder, final int position) {
        //set date
        viewHolder.tv_view_bids_date.setText(drivers.get(position).getPivot().getCreated_at());
        //set title of the driver
        viewHolder.tv_view_bids_truck_name.setText(drivers.get(position).getFirst_name() + " "+ drivers.get(position).getLast_name());
        //set description send by driver
        if (!drivers.get(position).getPivot().getDescription().equals("null")) {
            viewHolder.tv_view_bids_description.setText(drivers.get(position).getPivot().getDescription());
        } else {
            viewHolder.tv_view_bids_description.setText("No Description Available...");
        }

        //set bid amount
        if (!drivers.get(position).getPivot().getBid_amount().equals("null")) {
            viewHolder.tv_view_bids_price.setText("AED " + drivers.get(position).getPivot().getBid_amount());
        } else {
            viewHolder.tv_view_bids_price.setText("AED " + drivers.get(position).getPivot().getShipment_amount());
        }

        //set assign job onclick listener
        viewHolder.btn_view_bids_assign_jobs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assignJob(drivers.get(position).getPivot().getShipment_id() ,drivers.get(position).getDriverId());
            }
        });

        //set view truck onclick listener
        viewHolder.btn_view_bids_view_truck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewTruckActivity.class);
                intent.putExtra("id", drivers.get(position).getDriverId());
                context.startActivity(intent);
            }
        });

        //Set Driver Image
        String imageUrl = drivers.get(position).getDriver_image();
        if (!imageUrl.equals("null")) {
            ImageLoadTask imageLoadTask = new ImageLoadTask(imageUrl, viewHolder.img_view_bids_truck);
            imageLoadTask.execute();
        } else {
            viewHolder.img_view_bids_truck.setImageDrawable(context.getResources().getDrawable(R.color.colorHeading));
        }
    }

    public void onCompleteAssignment() {
        Intent intent = new Intent();
        intent.putExtra("isChanged", "changed");
        ((ViewBidsActivity)context).setResult(500, intent);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        Dialog dialog = builder.setMessage("Job Assigned Successful.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((ViewBidsActivity)context).finish();
                    }
                })
                .setCancelable(false)
                .create();
        dialog.show();
    }


    @Override
    public int getItemCount() {
        Log.d(TAG, "driver Size: "+drivers.size());
        return drivers.size();
    }

    public void swapData(ArrayList<Driver> drivers) {
        this.drivers = drivers;
        notifyDataSetChanged();
    }

    public void assignJob(final int shipmentId,final int driverId) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            //showSnackBar();
            ((BaseActivity)context).showSnackbar();
            return;
        }
        final StringRequest historyRequest = new StringRequest(Request.Method.POST, BaseActivity.API + "/user/assign_job",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("assignJob: Response", response);
                        ((BaseActivity)context).hideProgressdialog();
                        try{
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            if (status.equals("success")) {
                                Toast.makeText(context, "Job Assigned successful", Toast.LENGTH_SHORT).show();
                                onCompleteAssignment();
                            }
                        }catch (JSONException e) {
                            Log.d("ViewBidsAdapter", e.getLocalizedMessage(), e);
                        }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("Current ", error.getMessage(), error);
                            ((BaseActivity)context).hideProgressdialog();
                        }
                    }){

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("shipment_id", Integer.toString(shipmentId));
                    params.put("driver_id", Integer.toString(driverId));
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Authorization", "Bearer "+  BaseActivity.getSharedPref(context, BaseActivity.TOKEN));
                    return headers;

                }
            };

        ((BaseActivity)context).showProgressdialog("Loading...", false);
            VolleySingleton.getInstance(context).addToRequestQueue(historyRequest);
    }

//    private void showSnackBar() {
//        Snackbar snackbar = Snackbar.make(((ViewBidsActivity) context).findViewById(R.id.lyt_activity_container), "No internet connection", Snackbar.LENGTH_LONG)
//                .setAction("Settings", new View.OnClickListener(){
//                    @Override
//                    public void onClick(View v) {
//                        context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
//                    }
//                });
//        snackbar.show();
//    }


    public class ViewBidsViewHolder extends RecyclerView.ViewHolder{
        ImageView img_view_bids_truck;
        TextView tv_view_bids_truck_name;
        TextView tv_view_bids_description;
        TextView tv_view_bids_date;
        TextView tv_view_bids_price;
        Button btn_view_bids_view_truck;
        Button btn_view_bids_assign_jobs;

        public ViewBidsViewHolder(View view){
            super(view);
            img_view_bids_truck = view.findViewById(R.id.img_view_bids_truck);
            tv_view_bids_truck_name = view.findViewById(R.id.tv_view_bids_truck_name);
            tv_view_bids_description = view.findViewById(R.id.tv_view_bids_description);
            tv_view_bids_date = view.findViewById(R.id.tv_view_bids_date);
            tv_view_bids_price = view.findViewById(R.id.tv_view_bids_price);
            btn_view_bids_view_truck = view.findViewById(R.id.btn_view_bids_view_truck);
            btn_view_bids_assign_jobs = view.findViewById(R.id.btn_view_bids_assign_jobs);
        }
    }

    public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

        String url;
        ImageView imageView;

        public ImageLoadTask(String url, ImageView imageView) {
            this.url = url;
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                URL urlConnection = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlConnection.openConnection();
                connection.setDoOutput(true);
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                Bitmap imageBitmap = BitmapFactory.decodeStream(inputStream);
                return imageBitmap;
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
            imageView.setTag(url);
        }
    }
}


