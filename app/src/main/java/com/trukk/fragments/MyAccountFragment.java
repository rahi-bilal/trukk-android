package com.trukk.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.util.IOUtils;
import com.trukk.BaseActivity;
import com.trukk.ChangePasswordActivity;
import com.trukk.LoginActivity;
import com.trukk.R;
import com.trukk.volleyrequest.VolleyMultipartRequest;

import com.trukk.volleyrequest.VolleySingleton;
import com.yalantis.ucrop.UCrop;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class MyAccountFragment extends BaseFragment implements View.OnClickListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "MyAccountFragment";
    private boolean isImageUpdated;
    private boolean removeUserImage;
    private Uri outputImageUri;

    private String mParam1;
    private String mParam2;

    private View view;
    private Fragment fragment;
    private TextView tvProfileName, tv_edit_profile, tv_edit_profile_image, tv_change_pass;
    private EditText edtMobile, edtFname, edtLname;
    private ImageView img_profile_photo;
    private Bitmap profile_img_bitmap;


    public MyAccountFragment() {}

    public static MyAccountFragment newInstance(String param1, String param2) {
        MyAccountFragment fragment = new MyAccountFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
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
        view = inflater.inflate(R.layout.layout_profile, container, false);

        findViewsById();

        setOnclickListeners();

        //initialize flags
        isImageUpdated = false;
        removeUserImage = false;

        //setting text to fields
        String fname = BaseActivity.getSharedPref(context, BaseActivity.USER_FNAME);
        String lname = BaseActivity.getSharedPref(context, BaseActivity.USER_LNAME);
        String mobile = BaseActivity.getSharedPref(context, BaseActivity.USER_MOBILE);
        tvProfileName.setText(getString(R.string.full_name, fname, lname));
        edtFname.setText(fname);
        edtLname.setText(lname);
        edtMobile.setText(mobile);

        //setting up profile image
        String imageUri = BaseActivity.getSharedPref(getContext(), BaseActivity.USER_IMAGE);
        if (imageUri != null) {
            ImageLoadTask imageLoadTask = new ImageLoadTask(imageUri, img_profile_photo);
            imageLoadTask.execute();
        }

        //setting up profile image
        File file = new File(getActivity().getFilesDir(), "profile_image");
        outputImageUri = Uri.fromFile(file);
        fragment = this;
        return view;
    }


    public void findViewsById(){
        tvProfileName = view.findViewById(R.id.profile_name);
        edtFname = view.findViewById(R.id.edt_first_name);
        edtLname = view.findViewById(R.id.edt_family_name);
        edtMobile = view.findViewById(R.id.edt_mobile);
        tv_edit_profile = view.findViewById(R.id.tv_edit_profile);
        tv_edit_profile_image = view.findViewById(R.id.tv_edit_profile_image);
        img_profile_photo = view.findViewById(R.id.img_profile_photo);
        tv_change_pass = view.findViewById(R.id.tv_change_pass);
    }

    public void setOnclickListeners() {
        view.findViewById(R.id.edt_logout).setOnClickListener(this);
        tv_edit_profile.setOnClickListener(this);
        tv_edit_profile_image.setOnClickListener(this);
        tv_change_pass.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edt_logout:               Log.d("MyAccountFragment: ", "Logout --> Clicked");
                                                BaseActivity.setSharedPref(context, BaseActivity.TOKEN, null);
                                                BaseActivity.setSharedPrefBoolean(context, BaseActivity.IS_LOGIN, false);
                                                Intent intent = new Intent(context, LoginActivity.class);
                                                startActivity(intent);
                                                break;
            case R.id.tv_edit_profile:          editButtonClicked();
                                                break;
            case R.id.tv_edit_profile_image:    startIntentForImagePickup();
                                                break;
            case R.id.tv_change_pass:           Log.d(TAG, "Change Pass clicked");
                                                Intent changePswdIntent = new Intent(context, ChangePasswordActivity.class);
                                                startActivity(changePswdIntent);
                                                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mListener.updateCurrentFragment("my_account_fragment");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_IMAGE_GALLERY) {
            try {
                if (resultCode == RESULT_OK) {
                    Uri selectedImageUri = data.getData();
                    img_profile_photo.setImageURI(outputImageUri);
                    UCrop.of(selectedImageUri, outputImageUri)
                            .withAspectRatio(1, 1)
                            .withMaxResultSize(240, 240)
                            .start(getContext(), fragment);
                }
            } catch (Exception e) {
                Log.e(TAG, "File select error", e);
            }
        }

        if (requestCode == UCrop.REQUEST_CROP) {
            Log.d(TAG,"Crop done, Uri "+outputImageUri.toString());
            img_profile_photo.setImageDrawable(getResources().getDrawable(R.drawable.tv_grey_border));
            img_profile_photo.setImageURI(outputImageUri);
            isImageUpdated = true;
        }
    }

    public void startIntentForImagePickup() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
    }

    public void editButtonClicked() {
        if (tv_edit_profile.getText().toString().equals(getResources().getString(R.string.save))) {
            tv_edit_profile.setText(getResources().getString(R.string.edit));
            edtFname.setEnabled(false);
            edtLname.setEnabled(false);
            edtFname.setSelection(0,0);
            tv_edit_profile_image.setVisibility(View.GONE);
            updateProfile();

        } else {
            //changing text of edit to Save
            tv_edit_profile.setText(getResources().getString(R.string.save));
            //enabling profile image edit
            tv_edit_profile_image.setVisibility(View.VISIBLE);

            //enabling other fields to edit
            edtFname.setEnabled(true);
            edtLname.setEnabled(true);
            edtFname.requestFocus();
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(edtFname, InputMethodManager.SHOW_IMPLICIT);
            edtFname.setSelection(0, (edtFname.getText().length()));
            edtLname.setSelection(0, (edtLname.getText().length()));
        }
    }

    public boolean validateFormData(){

        return true;
    }

    public void updateProfile(){
        if (!validateFormData()){
            return;
        }

        //checking internet connection
        if (!connectionAvailable()) {
            showSnackbar();
            return;
        }

        String url = BaseActivity.API +"/user/account/edit_profile";
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                hideProgressDialog();
                try {
                    String responseString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                    Log.d(TAG+ 241, responseString);
                    JSONObject jsonResponse = new JSONObject(responseString);
                    if (jsonResponse.getString("status").equals("success")) {
                        getNewProfileInfo();
                    } else {
                        jsonResponse.getString("message");
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getLocalizedMessage(), e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideProgressDialog();
                Log.e(TAG, error.getMessage(), error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization","Bearer "+  BaseActivity.getSharedPref(getContext(), BaseActivity.TOKEN));
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> params = new HashMap<>();
                params.put("first_name", edtFname.getText().toString());
                params.put("last_name", edtLname.getText().toString());
                if (removeUserImage) {
                    //params.put("remove_image", removeUserImage);
                }
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                if (isImageUpdated) {
                    File file = new File(getActivity().getFilesDir(), "profile_image");
                    try {
                        BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
                        byte[] imageData = IOUtils.toByteArray(is);
                        params.put("user_image", new DataPart("user_image", imageData));
                    } catch (IOException e) {
                        Log.d(TAG, e.getLocalizedMessage(), e);
                    }
                    Log.d(TAG, params.toString());
                    return params;
                }else {
                    Drawable drawable = img_profile_photo.getDrawable();
                    Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    byte[] imageData = outputStream.toByteArray();
                    params.put("user_image", new DataPart("user_image", imageData));
                    Log.d(TAG, params.toString());
                    return params;
                }

            }
        };

        showProgressDialog("Updating Profile", false);
        VolleySingleton.getInstance(getContext()).addToRequestQueue(multipartRequest);
    }

    public void getNewProfileInfo(){

        //checking internet connection
        if (!connectionAvailable()) {
            showSnackbar();
            return;
        }

        StringRequest profileRequest = new StringRequest(Request.Method.GET, BaseActivity.API + "/user/account/view",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response);
                        hideProgressDialog();
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            if (status.equals("success")) {
                                JSONObject jsonUser = jsonResponse.getJSONObject("user");
                                Log.d(TAG, jsonUser.toString());
                                BaseActivity.setSharedPref(getContext(),BaseActivity.USER_FNAME, jsonUser.getString("first_name"));
                                BaseActivity.setSharedPref(getContext(),BaseActivity.USER_LNAME, jsonUser.getString("last_name"));
                                BaseActivity.setSharedPref(getContext(),BaseActivity.USER_IMAGE, jsonUser.getString("user_image"));
                                tvProfileName.setText(getResources().getString(R.string.full_name, jsonUser.getString("first_name"), jsonUser.getString("last_name")));
                                showMessageDialog("Profile updated successful");

                            } else {
                                showMessageDialog("Something went wrong");
                            }
                        }catch (JSONException e) {
                            Log.e(TAG, e.getLocalizedMessage(), e);
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("LoginActivity ", error.getMessage(), error);
                        hideProgressDialog();
                        showMessageDialog(error.getLocalizedMessage());
                    }
                }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer "+  BaseActivity.getSharedPref(getContext(), BaseActivity.TOKEN));
                Log.d(TAG+ 346, headers.get("Authorization"));
                return headers;
            }
        };
        showProgressDialog("Saving Profile", false);
        VolleySingleton.getInstance(getContext()).addToRequestQueue(profileRequest);
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
                profile_img_bitmap = BitmapFactory.decodeStream(inputStream);
                return profile_img_bitmap;
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }


}
