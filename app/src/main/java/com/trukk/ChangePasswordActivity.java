package com.trukk;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.trukk.volleyrequest.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class ChangePasswordActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "ChangePasswordActivity";
    private TextView tv_not_match;
    private EditText edt_old_password, edt_new_password, edt_cnf_new_password;
    private Button btn_change_password;
    private ImageButton img_btn_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        findViewsById();

        setOnClickListeners();

        edt_new_password.addTextChangedListener(getTextWatcher());
        edt_cnf_new_password.addTextChangedListener(getTextWatcher());

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_change_password:      changePassword();
                                                break;
            case R.id.img_btn_back:             super.onBackPressed();
                                                break;
        }
    }

    private void findViewsById() {
        tv_not_match = findViewById(R.id.tv_not_match);
        edt_old_password = findViewById(R.id.edt_old_password);
        edt_new_password = findViewById(R.id.edt_new_password);
        edt_cnf_new_password = findViewById(R.id.edt_cnf_new_password);
        btn_change_password = findViewById(R.id.btn_change_password);
        img_btn_back = findViewById(R.id.img_btn_back);
    }

    private void setOnClickListeners() {
        btn_change_password.setOnClickListener(this);
        img_btn_back.setOnClickListener(this);
    }

    private TextWatcher getTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (edt_cnf_new_password.length() != 0 ) {
                    if (!edt_cnf_new_password.getText().toString().equals(edt_new_password.getText().toString())) {
                        tv_not_match.setVisibility(View.VISIBLE);
                    } else {
                        tv_not_match.setVisibility(View.GONE);
                    }
                } else {
                    tv_not_match.setVisibility(View.GONE);
                }
            }
        };
    }

    private boolean validateForm() {
        if (edt_old_password.length() == 0) {
            showMessageDialog("Please Enter Old Password");
            return false;
        }
        if (edt_new_password.length() == 0) {
            showMessageDialog("Please Enter New Password");
            return false;
        }
        if (edt_cnf_new_password.length() == 0) {
            showMessageDialog("Please Confirm new Password");
            return false;
        }
        if (tv_not_match.getVisibility() == View.VISIBLE) {
            showMessageDialog("Password not matched");
            return false;
        }
        return true;
    }

    private void changePassword() {
        if (!validateForm()){return;}

        //check internet connection
        if (!connectionAvailable()) {
            showSnackbar();
            return;
        }

        String url = API + "/user/account/change_password";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        hideProgressdialog();
                        Log.d(TAG,"Response: " + response);
                        try{
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            if (status.equals("success")) {
                                String token = jsonResponse.getString(TOKEN);
                                setSharedPref(getBaseContext(), TOKEN, token);
                                showMessageDialog(jsonResponse.getString("message"));
                            } else {
                                showMessageDialog(jsonResponse.getString("message"));
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, e.getMessage(), e);
                        }
                        edt_old_password.setText("");
                        edt_new_password.setText("");
                        edt_cnf_new_password.setText("");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideProgressdialog();
                        Log.e(TAG, error.getMessage(), error);
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + getSharedPref(getBaseContext(), TOKEN));
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("old_password", edt_old_password.getText().toString());
                params.put("password", edt_new_password.getText().toString());
                params.put("password_confirmation", edt_cnf_new_password.getText().toString());
                return params;
            }
        };

        showProgressdialog("Changing Password", false);
        VolleySingleton.getInstance(this).addToRequestQueue(request);

    }
}
