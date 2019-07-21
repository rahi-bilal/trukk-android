package com.trukk.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.trukk.BaseActivity;
import com.trukk.R;
import com.trukk.interfaces.OnFragmentInteractionListener;
import com.trukk.volleyrequest.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ContactUsFragment extends BaseFragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "ContactUsFragment";

    private String mParam1;
    private String mParam2;

    private View view;
    private EditText edt_contact_name, edt_email_id, edt_phone_number, edt_subject, edt_message;
    private Button btn_contact_us_submit;

    private OnFragmentInteractionListener mListener;

    public ContactUsFragment() {
        // Required empty public constructor
    }

    public static ContactUsFragment newInstance(String param1, String param2) {
        ContactUsFragment fragment = new ContactUsFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_contact_us, container, false);

        findViewsById();

        //Submitting query on Submit Button Click
        btn_contact_us_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitQuery();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mListener.updateCurrentFragment("contact_us_fragment");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public boolean validateFormData(){
        if (edt_email_id.getText().length() == 0) {
            showMessageDialog("Please enter email address");
            return false;
        }
        if (edt_phone_number.getText().length() != 10) {
            showMessageDialog("Please enter valid mobile number");
            return false;
        }
        if (edt_subject.getText().length() == 0) {
            showMessageDialog("Please enter subject");
            return false;
        }
        if (edt_message.getText().length() == 0) {
            showMessageDialog("Please enter message");
            return false;
        }
        return true;
    }

    private void findViewsById() {
        edt_contact_name = view.findViewById(R.id.edt_contact_name);
        edt_subject = view.findViewById(R.id.edt_subject);
        edt_phone_number = view.findViewById(R.id.edt_phone_number);
        edt_email_id = view.findViewById(R.id.edt_email_id);
        edt_message = view.findViewById(R.id.edt_message);
        btn_contact_us_submit = view.findViewById(R.id.btn_contact_us_submit);
    }

    public void submitQuery(){
        //validating form
        if (!validateFormData()){ return; }

        //checking internet connection
        if (!connectionAvailable()) {
            showSnackbar();
            return;
        }

        StringRequest loginRequest = new StringRequest(Request.Method.POST, BaseActivity.API + "/contact",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response);
                        hideProgressDialog();
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            if (status.equals("success")) {
                                showMessageDialog(jsonResponse.getString("message"));
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
                        showMessageDialog(error.getMessage());
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("subject", edt_subject.getText().toString());
                params.put("mobile", edt_phone_number.getText().toString());
                params.put("email", edt_email_id.getText().toString());
                params.put("message", edt_message.getText().toString());

                if (edt_contact_name.getText().length() != 0) {
                    params.put("subject", edt_subject.getText().toString());
                }

                return params;
            }
        };
        showProgressDialog("Submitting Feedback", false);
        VolleySingleton.getInstance(getContext()).addToRequestQueue(loginRequest);
    }

}
