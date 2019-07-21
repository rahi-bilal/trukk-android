package com.trukk.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.trukk.BaseActivity;
import com.trukk.MapsActivity;
import com.trukk.R;
import com.trukk.media.AudioRecorder;
import com.trukk.models.Vehicle;
import com.trukk.volleyrequest.VolleyMultipartRequest;
import com.trukk.volleyrequest.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class ShipmentFragment extends BaseFragment implements View.OnClickListener, View.OnLongClickListener, View.OnTouchListener,
        MediaPlayer.OnCompletionListener {
    private static final String TAG = "ShipmentFragment";

    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    private static final String PICKUP_LOCATION = "pickup_location";
    private static final String PICKUP_TIME = "pickup_time";
    private static final String LOG_TAG = "ShipmentFragment";
    private static String fileName;
    private ArrayList vehicleList = new ArrayList();
    private ArrayList shipmentPhotos = new ArrayList();

    private View view;
    private View bottomSheetView;
    private View photoOptionBottomSheetView;
    private View lytDropOff;
    private BottomSheetDialog bottomSheetDialog;
    private BottomSheetDialog photoOptionBottomSheetDialog;
    private TextView tvCartoonDimensions;
    private TextView tvCartoonWeight;
    private TextView tvHold;
    private TextView tvHowManyLoading;
    private TextView tvHowManyUnLoading;
    private EditText edtDescription;
    private Button btnPreference;
    private ImageView imgMic;
    private ImageView imgPlayPause;
    private LinearLayout lytRecording;
    private LinearLayout lytPlayPause;
    private LinearLayout lytPreference;
    private LinearLayout lytImageContainer;
    private Chronometer chronometerPlayPause;

    private String pickupLocation;
    private String pickupTime;

    private int PACKAGING_ID = R.id.tv_cartoons;
    private int PREFERENCE_TAG = 0;
    private int LOADING_ID = R.id.tv_loading_none;
    private int LOADING_IMG_ID = R.id.img_loading_none;
    private int UNLOADING_ID = R.id.tv_unloading_none;
    private int UNLOADING_IMG_ID = R.id.img_unloading_none;
    private int PRICING_ID = R.id.tv_fixed_price;
    private int PAYMENT_METHOD_ID = 0;
    private boolean isPersonalLoadingRequired = false;
    private boolean isPersonalUnLoadingRequired = false;

    private double dropLat, dropLng;
    private String dropOffLocation;

    private AudioRecorder audioRecorder;


    public ShipmentFragment() {}

    public static ShipmentFragment newInstance(String param1, String param2) {
        ShipmentFragment fragment = new ShipmentFragment();
        Bundle args = new Bundle();
        args.putString(PICKUP_LOCATION, param1);
        args.putString(PICKUP_TIME, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            pickupLocation = getArguments().getString(PICKUP_LOCATION);
            pickupTime = getArguments().getString(PICKUP_TIME);
        }
        audioRecorder = new AudioRecorder();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_shipment, container, false);

        findViewsById();

        //Show labour and handling layout CARGO V/S PERSONAL
        if (shipment_type.equals(getResources().getString(R.string.cargo))){
            view.findViewById(R.id.lyt_cargo).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.lyt_personal).setVisibility(View.VISIBLE);
            view.findViewById(R.id.tv_packaging).setVisibility(View.GONE);
            view.findViewById(R.id.lyt_packaging).setVisibility(View.GONE);
            view.findViewById(R.id.lyt_dimen_weight).setVisibility(View.GONE);
        }

        //Setting up bottomSheetDialog
        bottomSheetDialog = new BottomSheetDialog(getContext());
        ViewGroup containerView = view.findViewById(R.id.lyt_scroll_view);
        bottomSheetView = getActivity().getLayoutInflater().inflate(R.layout.layout_bottom_sheet, containerView);
        bottomSheetDialog.setContentView(bottomSheetView);

        //Setting up image upload option bottomSheetDialog
        photoOptionBottomSheetDialog = new BottomSheetDialog(getContext());
        photoOptionBottomSheetView = getActivity().getLayoutInflater().inflate(R.layout.photo_option_bottom_sheet, containerView);
        photoOptionBottomSheetDialog.setContentView(photoOptionBottomSheetView);

        //Setting photo option bottom sheet dialog icons
        try {
            PackageManager packageManager = getActivity().getPackageManager();
            ImageView imageView = photoOptionBottomSheetView.findViewById(R.id.img_camera);
            //setting camera icon
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            String packageName = intent.resolveActivity(packageManager).getPackageName();
            imageView.setImageDrawable(packageManager.getApplicationIcon(packageName));
            //setting gallery icon
            imageView = photoOptionBottomSheetView.findViewById(R.id.img_gallery);
            intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            packageName = intent.resolveActivity(packageManager).getPackageName();
            imageView.setImageDrawable(packageManager.getApplicationIcon(packageName));
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(LOG_TAG, e.getLocalizedMessage(), e);
        }

        loadVehicleList();

        addOnClickListeners();

        //Asking for audio recording permissions
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            Log.d(LOG_TAG, "Requesting RECORD_AUDIO permissions");
            ActivityCompat.requestPermissions(getActivity(), permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        }

        //setting file path for recording audio message
        fileName = getActivity().getExternalCacheDir().getAbsolutePath() +"/instruction_message.mp4";

        //initialize packaging
        TextView tvPackaging = view.findViewById(PACKAGING_ID);
        packaging = "none";

        //initializing labour and handling
        labour_handling = "none";
        unload_handling = "none";

        //returning inflated layout for Shipment fragment
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        mListener.updateCurrentFragment("shipment_fragment");
    }

    //Finding Views in the layout
    private void findViewsById() {
        edtDescription = view.findViewById(R.id.edt_description);
        lytDropOff = view.findViewById(R.id.lyt_drop_off);
        tvCartoonDimensions = view.findViewById(R.id.tv_cartoon_dimensions);
        tvCartoonWeight = view.findViewById(R.id.tv_cartoon_weight);
        btnPreference = view.findViewById(R.id.btn_preference);
        tvHowManyLoading = view.findViewById(R.id.tv_how_many_loading);
        tvHowManyUnLoading = view.findViewById(R.id.tv_how_many_unloading);
        imgMic = view.findViewById(R.id.img_mic);
        tvHold = view.findViewById(R.id.tv_hold);
        lytRecording = view.findViewById(R.id.lyt_recording);
        lytPlayPause = view.findViewById(R.id.lyt_play_pause);
        imgPlayPause = lytPlayPause.findViewById(R.id.img_play_pause);
        chronometerPlayPause = lytPlayPause.findViewById(R.id.chronometer_play_pause);
        lytPreference = view.findViewById(R.id.lyt_preference);
        lytImageContainer = view.findViewById(R.id.lyt_image_container);
    }

    //Adding on click listeners
    private void addOnClickListeners() {
        //Drop Off Location
        view.findViewById(R.id.lyt_drop_off).setOnClickListener(this);
        view.findViewById(R.id.edt_drop_off).setOnClickListener(this);

        //Packaging
        view.findViewById(R.id.tv_no_packaging).setOnClickListener(this);
        view.findViewById(R.id.tv_pallets).setOnClickListener(this);
        view.findViewById(R.id.tv_cartoons).setOnClickListener(this);
        view.findViewById(R.id.tv_packaging_other).setOnClickListener(this);

        //truck preference

        //handling at loading
        view.findViewById(R.id.lyt_loading_none).setOnClickListener(this);
        view.findViewById(R.id.lyt_loading_labour).setOnClickListener(this);
        view.findViewById(R.id.lyt_loading_forklift).setOnClickListener(this);
        view.findViewById(R.id.lyt_loading_other).setOnClickListener(this);

        //handling at un-loading
        view.findViewById(R.id.lyt_unloading_none).setOnClickListener(this);
        view.findViewById(R.id.lyt_unloading_labour).setOnClickListener(this);
        view.findViewById(R.id.lyt_unloading_forklift).setOnClickListener(this);
        view.findViewById(R.id.lyt_unloading_other).setOnClickListener(this);

        //how many floors
        view.findViewById(R.id.count_minus_loading).setOnClickListener(this);
        view.findViewById(R.id.count_plus_loading).setOnClickListener(this);
        view.findViewById(R.id.count_minus_unloading).setOnClickListener(this);
        view.findViewById(R.id.count_plus_unloading).setOnClickListener(this);

        //personal loading and un-loading
        view.findViewById(R.id.tv_personal_loading).setOnClickListener(this);
        view.findViewById(R.id.tv_personal_unloading).setOnClickListener(this);

        //upload image click listener
        view.findViewById(R.id.img_upload).setOnClickListener(this);

        //Bottom Sheets
        view.findViewById(R.id.btn_complete_shipment).setOnClickListener(this);
        bottomSheetView.findViewById(R.id.tv_receive_quotation).setOnClickListener(this);
        bottomSheetView.findViewById(R.id.tv_fixed_price).setOnClickListener(this);
        bottomSheetView.findViewById(R.id.tv_cod).setOnClickListener(this);
        bottomSheetView.findViewById(R.id.tv_card).setOnClickListener(this);
        bottomSheetView.findViewById(R.id.btn_bottom_sheet_done).setOnClickListener(this);

        //Photo Options Bottom Sheet
        photoOptionBottomSheetView.findViewById(R.id.lyt_camera).setOnClickListener(this);
        photoOptionBottomSheetView.findViewById(R.id.lyt_gallery).setOnClickListener(this);

        //Audio recording
        imgMic.setOnLongClickListener(this);
        imgMic.setOnTouchListener(this);
        imgPlayPause.setOnClickListener(this);
    }

    //Implementing View.OnClickListener methods
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edt_drop_off:
            case R.id.lyt_drop_off:             Intent intent=new Intent(getActivity(), MapsActivity.class);
                                                startActivityForResult(intent, PICKUP_LOCATION_REQUEST_CODE);
                                                break;
            case R.id.tv_no_packaging:
            case R.id.tv_pallets:
            case R.id.tv_cartoons:
            case R.id.tv_packaging_other:       updatePackaging(v.getId());
                                                break;
            case R.id.lyt_loading_none:
            case R.id.lyt_loading_labour:
            case R.id.lyt_loading_forklift:
            case R.id.lyt_loading_other:        updateLoadingHandling(v.getId());
                                                break;
            case R.id.lyt_unloading_none:
            case R.id.lyt_unloading_labour:
            case R.id.lyt_unloading_forklift:
            case R.id.lyt_unloading_other:      updateUnLoadingHandling(v.getId());
                                                break;
            case R.id.btn_complete_shipment:    bottomSheetDialog.show();
                                                break;
            case R.id.tv_receive_quotation:
            case R.id.tv_fixed_price:           updatePricing(v.getId());
                                                break;
            case R.id.tv_personal_loading:      updatePersonalLoading();
                                                break;
            case R.id.tv_personal_unloading:    updatePersonalUnLoading();
                                                break;
            case R.id.count_minus_loading:
            case R.id.count_plus_loading:
            case R.id.count_minus_unloading:
            case R.id.count_plus_unloading:     updateNoOfFloors(v.getId());
                                                break;
            case R.id.tv_cod:
            case R.id.tv_card:                  updatePaymentMethod(v.getId());
                                                break;
            case R.id.img_play_pause:           playPause();
                                                break;
            case R.id.btn_bottom_sheet_done:    sendShipmentRequest();
                                                break;
            case R.id.img_upload:               photoOptionBottomSheetDialog.show();
                                                break;
            case R.id.lyt_camera:               openCamera();
                                                return;
            case R.id.lyt_gallery:              openGallery();
                                                return;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()){
            case R.id.img_mic: {
                recordAudio();
                break;
            }
        }
        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()){
            case R.id.img_mic: {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                   tvHold.setVisibility(View.VISIBLE);
                   new Handler().postDelayed(new Runnable() {
                       @Override
                       public void run() {
                           tvHold.setVisibility(View.GONE);
                       }
                   }, 1000);
                }
                if (event.getAction() == MotionEvent.ACTION_UP){
                    stopRecording(event);
                }
                break;
            }

        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Snackbar snackbar = Snackbar.make(view.findViewById(R.id.lyt_shipment_container), "Audio Recording permission granted", Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else {
                    Snackbar snackbar = Snackbar.make(view.findViewById(R.id.lyt_shipment_container), "Audio Recording permission not granted", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICKUP_LOCATION_REQUEST_CODE) {
            TextView tvPickupLocation = view.findViewById(R.id.edt_drop_off);
            dropLat = data.getDoubleExtra("lat", 0);
            dropLng = data.getDoubleExtra("lng", 0);
            dropOffLocation = data.getStringExtra("location");
            Log.d(LOG_TAG, "DropOff Latlng: " + dropLat + " , " + dropLng);
            if (dropLat != 0) {
                if (dropOffLocation != null) {
                    tvPickupLocation.setText(dropOffLocation);
                    drop_off_address = dropOffLocation;
                }

            }
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ImageView imageView =(ImageView) getActivity().getLayoutInflater().inflate(R.layout.shipment_image, lytImageContainer, false);
            imageView.setImageBitmap(imageBitmap);
            imageView.setTag(shipmentPhotos.size());
            lytImageContainer.addView(imageView);
            shipmentPhotos.add(imageBitmap);
        }

        if (requestCode == REQUEST_IMAGE_GALLERY) {
            try {
                if (resultCode == RESULT_OK) {

                    Uri selectedImageUri = data.getData();
                    // Set the image in ImageView
                    ImageView imageView =(ImageView) getActivity().getLayoutInflater().inflate(R.layout.shipment_image, lytImageContainer, false);
                    imageView.setImageURI(selectedImageUri);
                    imageView.setTag(shipmentPhotos.size());
                    lytImageContainer.addView(imageView);
                    InputStream is = getActivity().getContentResolver().openInputStream(selectedImageUri);
                    Drawable drawable = Drawable.createFromStream(is, selectedImageUri.toString());
                    Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                    shipmentPhotos.add(bitmap);
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "File select error", e);
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        chronometerPlayPause.setBase(SystemClock.elapsedRealtime());
        chronometerPlayPause.stop();
        imgPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_accent_24dp));
        AudioRecorder.isPlaying = false;
    }

    private View.OnClickListener getPreferenceClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout relativeLayout;
                TextView tvPreference;
                ImageView imgPreference;

                //Un-select already selected preference
                if (PREFERENCE_TAG != 0){
                    //Remove selected item
                    relativeLayout = view.findViewWithTag(PREFERENCE_TAG);
                    tvPreference = relativeLayout.findViewById(R.id.tv_preference);
                    imgPreference = relativeLayout.findViewById(R.id.img_preference);
                    imgPreference.setImageDrawable(getResources().getDrawable(R.drawable.circle_grey_border));
                    tvPreference.setTextColor(getResources().getColor(R.color.colorHeading));
                }

                if (PREFERENCE_TAG == (int)v.getTag()) {
                    btnPreference.setText(getResources().getString(R.string.noPreference));
                    PREFERENCE_TAG = 0;
                    vehicle_type_id = null;
                    return;
                }

                //Update new selected preference
                PREFERENCE_TAG = (int)v.getTag();
                vehicle_type_id = Integer.toString(PREFERENCE_TAG);
                relativeLayout = view.findViewWithTag(PREFERENCE_TAG);
                tvPreference = relativeLayout.findViewById(R.id.tv_preference);
                imgPreference = relativeLayout.findViewById(R.id.img_preference);
                imgPreference.setImageDrawable(getResources().getDrawable(R.drawable.circle_orange));
                tvPreference.setTextColor(getResources().getColor(android.R.color.white));
                btnPreference.setText(tvPreference.getText().toString());
            }
        };
    }

    private void updatePackaging(int id) {
        if (PACKAGING_ID == id){
            return;
        }
        //Un-select current Packaging option
        TextView tvPackagingOption = view.findViewById(PACKAGING_ID);
        tvPackagingOption.setBackground(getResources().getDrawable(R.drawable.bg_grey_border));
        tvPackagingOption.setTextColor(getResources().getColor(R.color.colorHeading));

        //Hide edtPackagingOther
        if (PACKAGING_ID == R.id.tv_packaging_other) {
            EditText edtPackagingOther = view.findViewById(R.id.edt_packaging_other);
            Animation animSlideOutTop = AnimationUtils.loadAnimation(getContext(), R.anim.sliide_out_top);
            edtPackagingOther.startAnimation(animSlideOutTop);
            edtPackagingOther.setVisibility(View.GONE);
        }

        //Update new selected PACKAGING_ID
        PACKAGING_ID = id;

        //Select new Packaging option
        tvPackagingOption = view.findViewById(PACKAGING_ID);
        tvPackagingOption.setBackground(getResources().getDrawable(R.drawable.btn_orange_bg));
        tvPackagingOption.setTextColor(getResources().getColor(android.R.color.white));

        View lytDimenWeight = view.findViewById(R.id.lyt_dimen_weight);
        switch (PACKAGING_ID) {
            case R.id.tv_no_packaging:  lytDimenWeight.setVisibility(View.GONE);
                                        break;
            case R.id.tv_pallets:       lytDimenWeight.setVisibility(View.VISIBLE);
                                        tvCartoonDimensions.setText(getActivity().getResources().getString(R.string.palletDimensions));
                                        tvCartoonWeight.setText(getActivity().getResources().getString(R.string.palletWeight));
                                        break;
            case R.id.tv_cartoons:      lytDimenWeight.setVisibility(View.VISIBLE);
                                        tvCartoonDimensions.setText(getActivity().getResources().getString(R.string.cartonDimensions));
                                        tvCartoonWeight.setText(getActivity().getResources().getString(R.string.cartonWeight));
                                        break;
            case R.id.tv_packaging_other:   lytDimenWeight.setVisibility(View.GONE);
                                            EditText edtPackagingOther = view.findViewById(R.id.edt_packaging_other);
                                            edtPackagingOther.setVisibility(View.VISIBLE);
                                            Animation animSlideInTop = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_top);
                                            edtPackagingOther.startAnimation(animSlideInTop);
                                            break;
        }
        //set current selected packaging
        packaging = tvPackagingOption.getText().toString().toLowerCase();
    }

    private void loadVehicleList() {
        StringRequest vehicleListRequest = new StringRequest(Request.Method.GET, BaseActivity.API + "/vehicle/list",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        hideProgressDialog();
                        JSONObject jsonResponse = null;
                        String status = null;
                        try {
                            jsonResponse = new JSONObject(response);
                            status = jsonResponse.getString("status");
                            if (status.equals("success")) {
                                JSONArray data = jsonResponse.getJSONArray("data");
                                for (int i=0; i<data.length(); i++) {
                                    Vehicle vehicle = new Vehicle();
                                    JSONObject vehicleJSON = data.getJSONObject(i);
                                    vehicle.setId(vehicleJSON.getInt("id"));
                                    vehicle.setStatus(vehicleJSON.getInt("status"));
                                    vehicle.setVehicle_name(vehicleJSON.getString("vehicle_name"));
                                    vehicleList.add(vehicle);
                                    RelativeLayout view = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.layout_truck_preference, null, false);
                                    view.setTag(vehicle.getId());
                                    TextView tvPreference = view.findViewById(R.id.tv_preference);
                                    tvPreference.setText(vehicle.getVehicle_name());
                                    lytPreference.addView(view);
                                    view.setOnClickListener(getPreferenceClickListener());
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
                        hideProgressDialog();
                        Log.e(LOG_TAG, error.getMessage(), error);
                    }
                });
        showProgressDialog("Please wait...", false);
        VolleySingleton.getInstance(getContext()).addToRequestQueue(vehicleListRequest);
    }

    private void updateLoadingHandling(int id) {
        int textViewId = R.id.tv_loading_none;
        int imageViewId = R.id.img_loading_none;
        switch (id) {
            case R.id.lyt_loading_none:
                textViewId = R.id.tv_loading_none;
                imageViewId = R.id.img_loading_none;
                break;
            case R.id.lyt_loading_labour:
                textViewId = R.id.tv_loading_labour;
                imageViewId = R.id.img_loading_labour;
                break;
            case R.id.lyt_loading_forklift:
                textViewId = R.id.tv_loading_forklift;
                imageViewId = R.id.img_loading_forklift;
                break;
            case R.id.lyt_loading_other:
                textViewId = R.id.tv_loading_other;
                imageViewId = R.id.img_loading_other;
                break;
        }

        //Un-select current loading handling
        ImageView imgLoading = view.findViewById(LOADING_IMG_ID);
        TextView tvLoading = view.findViewById(LOADING_ID);
        imgLoading.setImageDrawable(getResources().getDrawable(R.drawable.tv_grey_border));
        tvLoading.setTextColor(getResources().getColor(R.color.colorHeading));

        //Update new selected Loading Handling ID
        LOADING_ID = textViewId;
        LOADING_IMG_ID = imageViewId;

        //Select new Loading Handling
        imgLoading = view.findViewById(LOADING_IMG_ID);
        tvLoading = view.findViewById(LOADING_ID);
        imgLoading.setImageDrawable(getResources().getDrawable(R.drawable.tv_orange_bg));
        tvLoading.setTextColor(getResources().getColor(android.R.color.white));

        //update labour_handling
        labour_handling = tvLoading.getText().toString();

    }

    private void updateUnLoadingHandling(int id) {
        int textViewId = R.id.tv_unloading_none;
        int imageViewId = R.id.img_unloading_none;
        switch (id) {
            case R.id.lyt_unloading_none:
                textViewId = R.id.tv_unloading_none;
                imageViewId = R.id.img_unloading_none;
                break;
            case R.id.lyt_unloading_labour:
                textViewId = R.id.tv_unloading_labour;
                imageViewId = R.id.img_unloading_labour;
                break;
            case R.id.lyt_unloading_forklift:
                textViewId = R.id.tv_unloading_forklift;
                imageViewId = R.id.img_unloading_forklift;
                break;
            case R.id.lyt_unloading_other:
                textViewId = R.id.tv_unloading_other;
                imageViewId = R.id.img_unloading_other;
                break;
        }

        //Un-select current loading handling
        ImageView imgLoading = view.findViewById(UNLOADING_IMG_ID);
        TextView tvLoading = view.findViewById(UNLOADING_ID);
        imgLoading.setImageDrawable(getResources().getDrawable(R.drawable.tv_grey_border));
        tvLoading.setTextColor(getResources().getColor(R.color.colorHeading));

        //Update new selected Loading Handling ID
        UNLOADING_ID = textViewId;
        UNLOADING_IMG_ID = imageViewId;

        //Select new Loading Handling
        imgLoading = view.findViewById(UNLOADING_IMG_ID);
        tvLoading = view.findViewById(UNLOADING_ID);
        imgLoading.setImageDrawable(getResources().getDrawable(R.drawable.tv_orange_bg));
        tvLoading.setTextColor(getResources().getColor(android.R.color.white));

        //update unload_handling
        unload_handling = tvLoading.getText().toString();

    }

    private void updatePersonalLoading(){
        TextView tvPersonalLoading = view.findViewById(R.id.tv_personal_loading);
        if (isPersonalLoadingRequired){
            //Un-select personal loading
            tvPersonalLoading.setBackground(getResources().getDrawable(R.drawable.bg_grey_border));
            tvPersonalLoading.setTextColor(getResources().getColor(android.R.color.black));
            view.findViewById(R.id.tv_how_many_loading).setVisibility(View.GONE);
            view.findViewById(R.id.lyt_how_many_count_loading).setVisibility(View.GONE);
            isPersonalLoadingRequired = false;
        }else {
            tvPersonalLoading.setBackground(getResources().getDrawable(R.drawable.btn_orange_bg));
            tvPersonalLoading.setTextColor(getResources().getColor(android.R.color.white));
            view.findViewById(R.id.tv_how_many_loading).setVisibility(View.VISIBLE);
            view.findViewById(R.id.lyt_how_many_count_loading).setVisibility(View.VISIBLE);
            isPersonalLoadingRequired = true;
        }
    }

    private void updatePersonalUnLoading(){
        TextView tvPersonalUnLoading = view.findViewById(R.id.tv_personal_unloading);
        if (isPersonalUnLoadingRequired){
            //Un-select personal loading
            tvPersonalUnLoading.setBackground(getResources().getDrawable(R.drawable.bg_grey_border));
            tvPersonalUnLoading.setTextColor(getResources().getColor(android.R.color.black));
            view.findViewById(R.id.tv_how_many_unloading).setVisibility(View.GONE);
            view.findViewById(R.id.lyt_how_many_count_unloading).setVisibility(View.GONE);
            isPersonalUnLoadingRequired = false;
        }else {
            tvPersonalUnLoading.setBackground(getResources().getDrawable(R.drawable.btn_orange_bg));
            tvPersonalUnLoading.setTextColor(getResources().getColor(android.R.color.white));
            view.findViewById(R.id.tv_how_many_unloading).setVisibility(View.VISIBLE);
            view.findViewById(R.id.lyt_how_many_count_unloading).setVisibility(View.VISIBLE);
            isPersonalUnLoadingRequired = true;
        }
    }

    private void updatePricing(int pricingId) {

        //Return if same pricing is clicked again; Do nothing
        if (PRICING_ID == pricingId){
            return;
        }
        //Un-select current Pricing View
        TextView tvPricing = bottomSheetView.findViewById(PRICING_ID);
        tvPricing.setBackground(getResources().getDrawable(R.drawable.bg_grey_border));
        tvPricing.setTextColor(getResources().getColor(R.color.colorHeading));

        //Show/Hide Pricing Layout
        if (pricingId == R.id.tv_receive_quotation){
            bottomSheetView.findViewById(R.id.lyt_pricing).setVisibility(View.GONE);
        }else {
            bottomSheetView.findViewById(R.id.lyt_pricing).setVisibility(View.VISIBLE);
        }
        //Update current selected PRICING_ID
        PRICING_ID = pricingId;

        //Select new PRICING_ID view
        tvPricing = bottomSheetView.findViewById(PRICING_ID);
        tvPricing.setBackground(getResources().getDrawable(R.drawable.btn_orange_bg));
        tvPricing.setTextColor(getResources().getColor(android.R.color.white));
        if (PRICING_ID == R.id.tv_fixed_price) {
           pricing_type = "fixed";
        } else {
            pricing_type = "quotations";
        }
    }

    private String getPackagingDimensions() {
        EditText edtL = view.findViewById(R.id.edt_length);
        EditText edtW = view.findViewById(R.id.edt_width);
        EditText edtH = view.findViewById(R.id.edt_height);
        EditText edtWeight = view.findViewById(R.id.edt_cartoon_weight);
        EditText edtQty = view.findViewById(R.id.edt_qty);
        JSONObject dimensions = new JSONObject();
        try {
            dimensions.put("length", edtL.getText().toString());
            dimensions.put("width", edtW.getText().toString());
            dimensions.put("height", edtH.getText().toString());
            dimensions.put("weight", edtWeight.getText().toString());
            dimensions.put("qty", edtQty.getText().toString());
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        Log.d(LOG_TAG, dimensions.toString());
        return dimensions.toString();
    }

    private String getPersonalLabourAndHandling() {
        if(isPersonalLoadingRequired && isPersonalUnLoadingRequired) {
            return "both";
        }else if (isPersonalLoadingRequired) {
            return "loading";
        } else if (isPersonalUnLoadingRequired) {
            return "unloading";
        } else {
            return "none";
        }
    }

    private void updateNoOfFloors(int id){
        if ((id == R.id.count_minus_loading) || (id == R.id.count_plus_loading)){
            TextView textView =  view.findViewById(R.id.tv_no_of_floors_loading);
            int noOfFloorsLoading = Integer.parseInt(textView.getText().toString());
            if (id == R.id.count_plus_loading){
                textView.setText(String.format(Locale.getDefault(), "%d", ++noOfFloorsLoading));
            } else {
                if (noOfFloorsLoading == 0){
                    return;
                }
                textView.setText(String.format(Locale.getDefault(), "%d", --noOfFloorsLoading));
            }
        } else {
            TextView textView =  view.findViewById(R.id.tv_no_of_floors_unloading);
            int noOfFloorsUnLoading = Integer.parseInt(textView.getText().toString());
            if (id == R.id.count_plus_unloading){
                textView.setText(String.format(Locale.getDefault(), "%d", ++noOfFloorsUnLoading));
            } else {
                if (noOfFloorsUnLoading == 0){
                    return;
                }
                textView.setText(String.format(Locale.getDefault(), "%d", --noOfFloorsUnLoading));
            }
        }
    }

    private void updatePaymentMethod(int id){
        if (PAYMENT_METHOD_ID == id){
            return;
        }
        if (PAYMENT_METHOD_ID == 0){
            PAYMENT_METHOD_ID = id;
        }

        //Un-select current payment method textView
        TextView textView = bottomSheetView.findViewById(PAYMENT_METHOD_ID);
        textView.setBackground(getResources().getDrawable(R.drawable.bg_grey_border));
        textView.setTextColor(getResources().getColor(android.R.color.black));

        //Update new PAYMENT_METHOD_ID
        PAYMENT_METHOD_ID = id;

        //Select new payment method textView
        textView = bottomSheetView.findViewById(id);
        textView.setBackground(getResources().getDrawable(R.drawable.btn_orange_bg));
        textView.setTextColor(getResources().getColor(android.R.color.white));

        if (id == R.id.tv_cod) {
            order_type = "cod";
        } else {
            order_type = "prepaid";
        }
    }

    private void recordAudio(){
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            Log.d(LOG_TAG, "Requesting RECORD_AUDIO permissions");
            ActivityCompat.requestPermissions(getActivity(), permissions, REQUEST_RECORD_AUDIO_PERMISSION);
            return;
        }
        Log.d(LOG_TAG, "RECORD_AUDIO permission is granted");
        Log.d(LOG_TAG, "starting recording");
        audioRecorder.stopPlaying();
        audioRecorder.startRecording(fileName);
        lytPlayPause.setVisibility(View.GONE);
        lytRecording.setVisibility(View.VISIBLE);
        Chronometer chronometer = lytRecording.findViewById(R.id.chronometer);
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
    }

    private void stopRecording(MotionEvent event) {
        if (AudioRecorder.isRecording){
            Log.d(LOG_TAG, "stopping recording");
            audioRecorder.stopRecording();
            lytRecording.setVisibility(View.GONE);
            lytPlayPause.setVisibility(View.VISIBLE);
        }
    }

    private void playPause(){
        if (AudioRecorder.isPlaying){
            imgPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_accent_24dp));
            audioRecorder.stopPlaying();
            chronometerPlayPause.stop();
            chronometerPlayPause.setBase(SystemClock.elapsedRealtime());
        }else {
            Log.d(LOG_TAG, "playing recording");
            imgPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_primary_24dp));
            audioRecorder.startPlaying(fileName);
            chronometerPlayPause.setBase(SystemClock.elapsedRealtime());
            chronometerPlayPause.start();
            MediaPlayer mediaPlayer = audioRecorder.getMediaPlayer();
            mediaPlayer.setOnCompletionListener(this);
        }
    }

    private void openCamera() {
        photoOptionBottomSheetDialog.dismiss();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            Log.d(LOG_TAG, "starting camera...");
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void openGallery() {
        photoOptionBottomSheetDialog.dismiss();
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        Log.d(LOG_TAG, "Opening gallery...");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_IMAGE_GALLERY);
    }

    private Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio >1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        }else {
            height = maxSize;
            width =(int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private void showAlertDialog(String message, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(getContext())
                .setMessage(message)
                .setPositiveButton("OK", listener)
                .create().show();
    }

    private boolean validateForm() {
        drop_off_lat = dropLat;
        drop_off_long = dropLng;
        if (edtDescription.length() == 0) {
            showMessageDialog("Please enter Shipment Description");
            return false;
        }
        if (dropLat == 0 && dropLng == 0) {
            showMessageDialog("Please select DropOff location.");
            return false;
        }
        if (packaging == null) {
            showMessageDialog("Please select packaging.");
            return false;
        }
        if (pricing_type == null) {
            showMessageDialog("Please select Pricing");
            return false;
        }
        if (pricing_type.equals("fixed")) {
            EditText edtAmount = bottomSheetView.findViewById(R.id.edt_enter_amount);
            if (edtAmount.getText().toString().length() == 0) {
                showMessageDialog("Please enter pricing amount");
                return false;
            }
        }
        if (order_type == null) {
            showMessageDialog("Please select Payment method.");
            return false;
        }
        if (BaseActivity.getSharedPref(getContext(), BaseActivity.TOKEN) == null) {
            showMessageDialog("Your session expired, Please login again.");
            return false;
        }
        return true;
    }

    private void sendShipmentRequest() {
        if (!validateForm()) {
            return;
        }

        //checking internet connection
        if (!connectionAvailable()) {
            showSnackbar();
            return;
        }

        bottomSheetDialog.dismiss();

        String url = BaseActivity.API + "/user/create_shipping";
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                hideProgressDialog();
                if (response.statusCode == 500) {
                    showMessageDialog("Internal server error.");
                    return;
                }
                try {
                    String responseString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                    JSONObject jsonResponse = new JSONObject(responseString);
                    Log.d(LOG_TAG, "Response: " + jsonResponse);
                    String status = jsonResponse.getString("status");
                    if (status.equals("success")) {
                        showAlertDialog("Shipment details successfully submitted.", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mListener.onShipmentComplete();
                            }
                        });
                    } else {
                        String message = jsonResponse.getString("message");
                        showMessageDialog(message);
                    }
                } catch (Exception e) {
                    Log.d(LOG_TAG, e.getMessage(), e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideProgressDialog();
                try {
                    String errorString= new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers));
                    Log.d(LOG_TAG, "Error:" +  errorString);
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getLocalizedMessage());
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization","Bearer "+  BaseActivity.getSharedPref(getContext(), BaseActivity.TOKEN));
                return headers;

            }

            @Override
            protected Map<String, String> getParams(){
                Map<String, String> params = new HashMap<>();
                EditText edtDescription = view.findViewById(R.id.edt_description);
                EditText edtInstruction = view.findViewById(R.id.edt_instruction);
                EditText edtAmount = bottomSheetView.findViewById(R.id.edt_enter_amount);
                TextView tvNoOfFloorsLoading = view.findViewById(R.id.tv_no_of_floors_loading);
                TextView tvNoOfFloorsUnLoading = view.findViewById(R.id.tv_no_of_floors_unloading);

                //putting params ShipAnythingFragment
                params.put("pickup_lat", Double.toString(pickup_lat));
                params.put("pickup_long", Double.toString(pickup_long));
                params.put("pickup_address", pickup_address);
                params.put("pickup_time" , pickup_time);
                params.put( "shipment_type", shipment_type.toLowerCase());
                //putting params ShipmentFragment
                params.put("description", edtDescription.getText().toString());
                params.put("drop_off_lat", Double.toString(drop_off_lat));
                params.put("drop_off_long", Double.toString(drop_off_long));
                params.put("drop_off_address", drop_off_address);
                params.put("packaging", packaging.toLowerCase());
                //putting truck preference params
                if (vehicle_type_id != null){
                    params.put("vehicle_type_id", vehicle_type_id);
                }
                //putting labour and handling
                if (shipment_type.toLowerCase().equals("cargo")) {
                    //cases for cargo shipment
                    params.put("packaging_dimensions", getPackagingDimensions());
                    params.put("labour_handling", labour_handling);
                    params.put("unload_handling", unload_handling);
                } else {
                    //cases for personal shipment
                    params.put("personal_labour_handling", getPersonalLabourAndHandling());
                    params.put("floor_number_loading", tvNoOfFloorsLoading.getText().toString());
                    params.put("floor_number_unloading", tvNoOfFloorsUnLoading.getText().toString());
                }

                params.put("instructions", edtInstruction.getText().toString());

                //setting pricing params
                params.put("pricing_type", pricing_type);
                if (pricing_type.equals("fixed")) {
                    params.put("amount", edtAmount.getText().toString());
                }
                params.put("order_type", order_type);

                Log.d(LOG_TAG, "getParams: "+params.toString());
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData(){
                Map<String, DataPart> params = new HashMap<>();
                Log.d(LOG_TAG, "creating getByteData params");
                //Add audio instruction. if any?
                if (audioRecorder.isRecorded) {
                    File file = new File(fileName);
                    int size =(int) file.length();
                    byte[] bytes = new byte[size];
                    try {
                        BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                        buf.read(bytes, 0, size);
                        buf.close();
                        params.put("voice_instructions", new DataPart("voice_instructions.mp4", bytes));

                    } catch (Exception e) {
                        Log.e(LOG_TAG, "getByteData(): ", e);
                    }
                }

                //add images here if any
                if(shipment_type.toLowerCase().equals("personal")) {
                    for (int i=0; i<shipmentPhotos.size(); i++) {
                        Bitmap bitmap = (Bitmap) shipmentPhotos.get(i);
                        bitmap = getResizedBitmap(bitmap, 480);
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                        byte[] imageByteArray = byteArrayOutputStream.toByteArray();
                        params.put("shipment_image["+i+"]", new DataPart("shipment_image[" + i + "]", imageByteArray));
                    }
                }

                Log.d(LOG_TAG, "data params" +  params.toString());
                return params;
            }
        };
        showProgressDialog("Submitting request...", false);
        VolleySingleton.getInstance(getContext()).addToRequestQueue(multipartRequest);

    }
}
