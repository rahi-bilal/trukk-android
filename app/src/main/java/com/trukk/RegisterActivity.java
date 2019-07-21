package com.trukk;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.trukk.utils.MessageDialog;
import com.trukk.volleyrequest.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends BaseActivity implements View.OnClickListener {

    private TextView tvYouHave;
    private EditText edtMobile, edtFname, edtLname, edtEmail, edtPassword;
    private EditText edtOTP;
    private TextView tvSendAgain;
    private Button btnSignup, btnOTP;
    private static final String LOG_TAG = "RegisterActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        }

        setContentView(R.layout.activity_register);

        findViewsById();

        setOnClickListeners();
        //Setting String of you have an account
        String text = "<font color=#ffffff>You have an account. </font> <font color=#F28020>Login</font>";
        tvYouHave.setText(Html.fromHtml(text));

        //Setting String of resend otp
        text = "<font color=#ffffff>Didn\'t get OTP?</font> <font color=#F28020> Send again.</font>";
        tvSendAgain.setText(Html.fromHtml(text));

        //Setting Activity heading text view text
        if (getIntent().getStringExtra("mobile") != null) {
            showOtpLayout();
            setSharedPref(this, USER_MOBILE, getIntent().getStringExtra("mobile"));
            Log.d("Mobile: ", getSharedPref(getApplicationContext(), USER_MOBILE));
            resendOTP();
        } else {
            updateUI();
        }
    }


    private void findViewsById() {
        tvYouHave = findViewById(R.id.tv_you_have);
        edtMobile = findViewById(R.id.edt_mobile_no);
        edtFname = findViewById(R.id.edt_first_name);
        edtLname = findViewById(R.id.edt_family_name);
        edtEmail = findViewById(R.id.edt_email_id);
        edtPassword = findViewById(R.id.edt_password);
        btnSignup = findViewById(R.id.btn_signup);
        edtOTP = findViewById(R.id.edt_otp);
        tvSendAgain = findViewById(R.id.tv_send_again);
        btnOTP = findViewById(R.id.btn_otp);

    }

    private void setOnClickListeners() {
        tvYouHave.setOnClickListener(this);
        btnSignup.setOnClickListener(this);
        tvSendAgain.setOnClickListener(this);
        btnOTP.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_you_have:      finish();
                                        break;
            case R.id.btn_signup:       signUp();
                                        break;
            case R.id.btn_otp:          verifyMobile();
                                        break;
            case R.id.tv_send_again:    resendOTP();
                                        break;
        }
    }

    public void showAlertDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Dialog dialog = builder.setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resendOTP();
                    }
                })
                .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        dialog.show();

    }

    public void updateUI() {
        if(getSharedPrefBoolean(getBaseContext(), IS_LOGIN)) {
            if (getSharedPref(getBaseContext(), ACCOUNT_STATUS).equals("pending")){
                TextView tvRegister = findViewById(R.id.tv_register);
                tvRegister.setText("Verify Mobile");
                findViewById(R.id.lyt_register_fields).setVisibility(View.GONE);
                findViewById(R.id.lyt_otp).setVisibility(View.VISIBLE);
                return;
            }

        }
    }

    public void showOtpLayout() {
        TextView tvRegister = findViewById(R.id.tv_register);
        tvRegister.setText("Verify Mobile");
        findViewById(R.id.lyt_register_fields).setVisibility(View.GONE);
        findViewById(R.id.lyt_otp).setVisibility(View.VISIBLE);
    }

    public void resendOTP(){
        //check internet connection
        if (!connectionAvailable()) {
            showSnackbar();
            return;
        }

        String url = API +"/user/auth/resend-otp";
        StringRequest otpRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        hideProgressdialog();
                        Log.d(LOG_TAG, response);
                        try{
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getString("status").equals("success")) {
                                String message = jsonResponse.optString("message");
                                showMessageDialog(message);
                                updateUI();
                                return;
                            }
                            if(jsonResponse.getString("status").equals("failed")) {
                                String message = jsonResponse.optString("message");
                                if (message != null){
                                    showMessageDialog(message);
                                }
                            }
                        }catch (JSONException e) {
                            showMessageDialog("Response parsing error");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideProgressdialog();
                        showMessageDialog(error.getMessage());

                    }
                }){
            @Override
            protected Map<String, String> getParams(){
                HashMap<String, String> params = new HashMap();
                params.put("mobile", getSharedPref(getApplicationContext(), USER_MOBILE));
                Log.d("getParams: ", params.toString());
                return params;
            }
        };
        showProgressdialog("Sending OTP", false);
        VolleySingleton.getInstance(this).addToRequestQueue(otpRequest);
    }

    public void signUp(){
        if (!validateFormData()){
            return;
        }
        StringRequest loginRequest = new StringRequest(Request.Method.POST, API + "/user/auth/register",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        hideProgressdialog();
                        try{
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            Log.d(LOG_TAG, response);
                            if (status.equals("success")){
                                Log.d(LOG_TAG, status);
                                JSONObject data = jsonResponse.getJSONObject("data");
                                setSharedPref(getBaseContext(), USER_ID, data.getString("id"));
                                setSharedPref(getBaseContext(), USER_FNAME, data.getString("first_name"));
                                setSharedPref(getBaseContext(), USER_LNAME, data.getString("last_name"));
                                setSharedPref(getBaseContext(), USER_EMAIL, data.getString("email"));
                                setSharedPref(getBaseContext(), USER_MOBILE, data.getString("mobile"));
                                setSharedPref(getBaseContext(), ACCOUNT_STATUS, "pending");
                                setSharedPrefBoolean(getBaseContext(), IS_LOGIN, true);
                                showAlertDialog("Successfully registered.\nTo continue please verify your mobile number.");
                            } else {
                                JSONObject errorsObject = jsonResponse.optJSONObject("errors");
                                if(errorsObject!= null){
                                    JSONArray mobileErrorArray = errorsObject.optJSONArray("mobile");
                                    Log.d(LOG_TAG, mobileErrorArray.toString());
                                    String er = mobileErrorArray.getString(0);
                                    if (er!= null){
                                        showMessageDialog(er);
                                        return;
                                    }
                                }
                                String message = jsonResponse.optString("message");
                                if (message != null){
                                    Log.d(LOG_TAG, message);
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
                        Log.d("LoginActivity ", error.getMessage());
                        hideProgressdialog();
                        Snackbar snackbar = Snackbar.make(findViewById(R.id.lyt_activity_container), error.getMessage(), Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("mobile", edtMobile.getText().toString());
                params.put("first_name", edtFname.getText().toString());
                params.put("last_name", edtLname.getText().toString());
                params.put("email", edtEmail.getText().toString());
                params.put("device_type", "android");
                params.put("device_id", getDeviceId());
                params.put("password", edtPassword.getText().toString());
                Log.d(LOG_TAG, params.toString());
                return params;
            }
        };
        showProgressdialog("Signing Up", false);
        VolleySingleton.getInstance(this).addToRequestQueue(loginRequest);
    }

    private void verifyMobile() {
        if (edtOTP.getText().toString().length() == 0){
            showMessageDialog("Please enter OTP.");
            return;
        }
        String otp = edtOTP.getText().toString();
        String url = API + "/user/auth/verify_mobile";
        StringRequest verifyMobileRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        hideProgressdialog();
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            Log.d(LOG_TAG, response);
                            if (status.equals("success")){
                                JSONObject data = jsonResponse.getJSONObject("data");
                                setSharedPref(getBaseContext(), USER_ID, data.getString("id"));
                                setSharedPref(getBaseContext(), USER_FNAME, data.getString("first_name"));
                                setSharedPref(getBaseContext(), USER_LNAME, data.getString("last_name"));
                                setSharedPref(getBaseContext(), USER_EMAIL, data.getString("email"));
                                setSharedPref(getBaseContext(), USER_MOBILE, data.getString("mobile"));
                                setSharedPref(getBaseContext(), ACCOUNT_STATUS, data.getString("account_status"));
                                setSharedPref(getBaseContext(), TOKEN, jsonResponse.getString("token"));
                                Log.d(LOG_TAG, "Token: "+ jsonResponse.getString("token"));
                                setSharedPrefBoolean(getBaseContext(), IS_LOGIN, true);
                                Toast.makeText(getBaseContext(), "Mobile Verified.", Toast.LENGTH_SHORT).show();
                                startMain();
                            } else {
                                String message = jsonResponse.getString("message");
                                if(message != null) {
                                    showMessageDialog(message);
                                }
                            }
                        } catch (Exception e) {
                            Log.d(LOG_TAG, e.getLocalizedMessage());
                            showMessageDialog(e.getLocalizedMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                       hideProgressdialog();
                       Log.d(LOG_TAG, error.getLocalizedMessage());
                       showMessageDialog(error.getLocalizedMessage());
                    }
                }
            ){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("mobile", getSharedPref(getBaseContext(), USER_MOBILE));
                    params.put("mobile_otp", edtOTP.getText().toString());
                    return params;
                }
        };
        showProgressdialog("Verifying OTP", false);
        VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(verifyMobileRequest);
    }

    private boolean validateFormData() {
        if(edtMobile.getText().toString().length() != 10) {
            edtMobile.setError("10-digits required");
            return false;
        }
        if (edtFname.getText().toString().length() == 0){
            edtFname.setError("Field cannot be empty");
            return false;
        }
        if (edtLname.getText().toString().length() == 0){
            edtLname.setError("Field cannot be empty");
            return false;
        }
        if (edtEmail.getText().toString().length() == 0){
            edtEmail.setError("Field cannot be empty");
            return false;
        }
        if (edtPassword.getText().toString().length() < 8){
            edtPassword.setError("At least 8-characters required");
            return false;
        }
        return true;
    }

    private void startMain() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
