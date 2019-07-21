package com.trukk;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.trukk.volleyrequest.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ForgotPasswordActvity extends BaseActivity {

    private static final String LOG_TAG = "ForgetPasswordActivity";

    Button btn_next1, btn_next2, btn_next3;
    ImageView img_back;
    EditText edt_mobile, edt_otp, edt_pswd, edt_conf_pswd;
    RelativeLayout rl1, rl2, rl3;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        }

        setContentView(R.layout.activity_forgot_password);

        btn_next1 = findViewById(R.id.btn_next1);
        btn_next2 = findViewById(R.id.btn_next2);
        btn_next3 = findViewById(R.id.btn_next3);
        img_back = findViewById(R.id.img_back);
        rl1 = findViewById(R.id.rl1);
        rl2 = findViewById(R.id.rl2);
        rl3 = findViewById(R.id.rl3);
        edt_mobile = findViewById(R.id.et_mobile);
        edt_otp = findViewById(R.id.et_otp);
        edt_pswd = findViewById(R.id.et_new_pass);
        edt_conf_pswd = findViewById(R.id.et_new_cpass);

        btn_next1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (edt_mobile.getText().toString().length() != 10) {
                    showMessageDialog("10-digit mobile number required");
                    return;
                }
                resendOtp(edt_mobile.getText().toString());
            }
        });

        btn_next2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edt_otp.getText().toString().length()< 6) {
                    showMessageDialog("6-digit OTP required");
                    return;
                }
                rl3.setVisibility(View.VISIBLE);
                rl2.setVisibility(View.GONE);
                rl1.setVisibility(View.GONE);
            }
        });

        btn_next3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!edt_pswd.getText().toString().equals(edt_conf_pswd.getText().toString())) {
                    showMessageDialog("Password does not match.");
                }
                changePassword();
            }
        });

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    public void showAlertDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Dialog dialog = builder.setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(ForgotPasswordActvity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .create();
        dialog.show();
    }

    public void resendOtp(final String mobile){
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
                                rl2.setVisibility(View.VISIBLE);
                                rl1.setVisibility(View.GONE);
                                rl3.setVisibility(View.GONE);
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
                params.put("mobile", mobile);
                Log.d("getParams: ", params.toString());
                return params;
            }
        };
        showProgressdialog("Sending OTP", false);
        VolleySingleton.getInstance(this).addToRequestQueue(otpRequest);
    }

    public void changePassword() {
        //check internet connection
        if (!connectionAvailable()) {
            showSnackbar();
            return;
        }
        String url = API +"/user/auth/reset-password";
        StringRequest resetPaswdRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        hideProgressdialog();
                        Log.d(LOG_TAG, response);
                        try{
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getString("status").equals("success")) {
                                String message = jsonResponse.optString("message");
                                showAlertDialog(message);
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
                params.put("mobile", edt_mobile.getText().toString());
                params.put("mobile_otp", edt_otp.getText().toString());
                params.put("password", edt_pswd.getText().toString());
                Log.d("getParams: ", params.toString());
                return params;
            }
        };
        showProgressdialog("Resetting password", false);
        VolleySingleton.getInstance(this).addToRequestQueue(resetPaswdRequest);
    }

}
