package com.trukk;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.trukk.volleyrequest.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends BaseActivity implements View.OnClickListener{
    private TextView tvDontHave, tvForgotPassword;
    private EditText edtMobileNo, edtPassword;
    private Button btnLogin;

    private String text = "<font color=#ffffff>Don't have an account. </font> <font color=#F28020>Register</font>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        }

        setContentView(R.layout.activity_login);

        findViewsById();

        addOnClickListeners();

        tvDontHave.setText(Html.fromHtml(text));

    }

    public void findViewsById(){
        tvDontHave = findViewById(R.id.tv_dont_have);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        btnLogin = findViewById(R.id.btn_login);
        edtMobileNo = findViewById(R.id.edt_mobile_no);
        edtPassword = findViewById(R.id.edt_password);
    }

    public void addOnClickListeners(){
        tvDontHave.setOnClickListener(this);
        tvForgotPassword.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent i;
        switch (v.getId()){
            case R.id.tv_dont_have:         i = new Intent(LoginActivity.this , RegisterActivity.class);
                                            startActivity(i);
                                            break;
            case R.id.tv_forgot_password:   i = new Intent(LoginActivity.this , ForgotPasswordActvity.class);
                                            startActivity(i);
                                            break;
            case R.id.btn_login:            login();
                                            break;
        }
    }

    public void showAlertDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Dialog dialog = builder.setMessage(message)
                .setPositiveButton("Verify", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                        intent.putExtra("mobile", edtMobileNo.getText().toString());
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetSharedPref(getApplicationContext());
                        dialog.dismiss();
                    }
                })
                .create();
        dialog.show();
    }

    public void login(){
        if (!validateFormData()){
            return;
        }

        //check internet connection
        if (!connectionAvailable()) {
            showSnackbar();
            return;
        }

        StringRequest loginRequest = new StringRequest(Request.Method.POST, API + "/user/auth/login",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        hideProgressdialog();
                        try{
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            Log.d("LoginActivity: Response", response);
                            if (status.equals("success")){
                                Log.d("LoginActivity: Status", status);
                                JSONObject user = jsonResponse.getJSONObject("user");
                                setSharedPref(getBaseContext(), TOKEN, jsonResponse.getString("token"));
                                setSharedPref(getBaseContext(), USER_ID, user.getString("id"));
                                setSharedPref(getBaseContext(), USER_MOBILE, user.getString("mobile"));
                                setSharedPref(getBaseContext(), USER_FNAME, user.getString("first_name"));
                                setSharedPref(getBaseContext(), USER_LNAME, user.getString("last_name"));
                                setSharedPref(getBaseContext(), ACCOUNT_STATUS, user.getString("account_status"));
                                setSharedPref(getBaseContext(), USER_IMAGE, user.getString("user_image"));
                                setSharedPrefBoolean(getBaseContext(), IS_LOGIN, true);
                                startMain();
                            } else {
                                if (status.equals("error")) {
                                    showAlertDialog(jsonResponse.getString("message"));
                                    return;
                                }
                                JSONObject errorsObject = jsonResponse.optJSONObject("errors");
                                if(errorsObject!= null){
                                    JSONArray mobileErrorArray = errorsObject.optJSONArray("mobile");
                                    Log.d("Login Activity", mobileErrorArray.toString());
                                    String er = mobileErrorArray.getString(0);
                                    if (er!= null){
                                        Log.d("LoginActivity", er);
                                        showMessageDialog(er);
                                        return;
                                    }
                                }
                                String message = jsonResponse.optString("message");
                                if (message != null){
                                    Log.d("Login Activity", message);
                                    showMessageDialog(message);
                                    return;
                                }
                            }
                        }catch (JSONException e){
                            Log.d("LoginActivity", e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("LoginActivity ", error.getMessage(), error);
                        hideProgressdialog();
                        showMessageDialog(error.getMessage());
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("mobile", edtMobileNo.getText().toString());
                params.put("password", edtPassword.getText().toString());
                params.put("device_type", "android");
                params.put("device_id", getDeviceId());
                return params;
            }
        };
        showProgressdialog("Logging in", false);
        VolleySingleton.getInstance(this).addToRequestQueue(loginRequest);
    }

    public boolean validateFormData(){
        if (edtMobileNo.length() == 0) {
            showMessageDialog("please enter mobile number");
            return false;
        }
        if (edtPassword.length() == 0){
            showMessageDialog("Please enter password");
            return false;
        }
        return true;
    }


    public void startMain(){
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }
}
