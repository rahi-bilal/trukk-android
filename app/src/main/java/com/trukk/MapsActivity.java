package com.trukk;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.trukk.utils.Constants;

public class MapsActivity extends BaseActivity implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, View.OnClickListener, AdapterView.OnItemClickListener, GoogleMap.OnMyLocationChangeListener {


    private GoogleMap googleMap;
    protected Location lastLocation;

    private AddressResultReceiver resultReceiver;
    private EditText edtSearch;
    private ImageView imgSearchGoArrow;
    private ImageView imgClearSearch;
    private ListView lvSearchLocation;
    private Button btnSetLocation;

    private LatLng[] searchLatLngs;
    private String[] searchResult;
    private static String searchText = "";
    private LocationManager locationManager;


    public static final int MY_LOCATION_REQUEST_CODE = 12;
    public static final int ACCESS_MY_LOCTION_REQUEST_CODE = 13;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        findViewsById();

        addOnClickListeners();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        resultReceiver = new AddressResultReceiver(new Handler());

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 400, 1000, this);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_LOCATION_REQUEST_CODE);
        }

    }

    @Override
    public void onBackPressed() {
        if (lvSearchLocation.getVisibility() == View.VISIBLE){
            BaseActivity.hideKeyboard(this);
            lvSearchLocation.setVisibility(View.GONE);
            return;
        }else{
            Intent intent = new Intent();
            intent.putExtra("RESULT", "");
            setResult(101, intent);
            finish();
        }
        super.onBackPressed();

    }

    private void findViewsById() {
        edtSearch = findViewById(R.id.edt_search_location);
        imgSearchGoArrow = findViewById(R.id.img_search_go);
        imgClearSearch = findViewById(R.id.img_clear_search);
        lvSearchLocation = findViewById(R.id.lv_search_location);
        btnSetLocation = findViewById(R.id.btn_set_location);
    }

    private void addOnClickListeners() {
        imgSearchGoArrow.setOnClickListener(this);
        imgClearSearch.setOnClickListener(this);
        btnSetLocation.setOnClickListener(this);

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (edtSearch.getText().length() != 0){
                    if (imgClearSearch.getVisibility() == View.GONE){
                        imgClearSearch.setVisibility(View.VISIBLE);
                    }
                }else {
                    if (imgClearSearch.getVisibility() == View.VISIBLE){
                        imgClearSearch.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals(searchText)){
                    return;
                }
                searchText = s.toString();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startIntentForLocationSearch();
                    }
                }, 500);
            }
        });
        edtSearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if ((keyCode == KeyEvent.KEYCODE_ENTER) && (event.getAction() == event.ACTION_DOWN)){
                    startIntentForLocationSearch();
                }
                if ((keyCode == KeyEvent.KEYCODE_BACK) && (event.getAction() == event.ACTION_DOWN)){
                    onBackPressed();
                }

                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_search_go:        startIntentForLocationSearch();
                                            break;
            case R.id.img_clear_search:     edtSearch.setText("");
                                            setSearchListViewAdapter(new String[]{});
                                            lvSearchLocation.setVisibility(View.GONE);
                                            break;
            case R.id.btn_set_location:     startIntentService();
                                            //sendResultToActivity();
                                            break;
        }
    }

    private void setSearchListViewAdapter(String[] contents) {
        ListAdapter listAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                contents);
        lvSearchLocation.setAdapter(listAdapter);
        lvSearchLocation.setOnItemClickListener(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.setOnMapClickListener(getOnMapClickListener());
        setCurrentLocationOnMap();
    }

    private void setCurrentLocationOnMap() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            googleMap.setOnMyLocationButtonClickListener(this);
            googleMap.setOnMyLocationClickListener(this);
            googleMap.setOnMyLocationChangeListener(this);
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(14));
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_LOCATION_REQUEST_CODE);
        }
    }

    private GoogleMap.OnMapClickListener getOnMapClickListener() {
        return new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                googleMap.clear();
                Location location = new Location("");
                location.setLatitude(latLng.latitude);
                location.setLongitude(latLng.longitude);
                lastLocation = location;
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
            }
        };
    }


    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(Location location) {
        lastLocation = location;
        if (lastLocation == null) {
            return;
        }
        startIntentService();

    }

    @Override
    public void onMyLocationChange(Location location) {

    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;

        //Place Current Location Marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        //Move Map Camera
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
        locationManager.removeUpdates(this);

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            if (permissions.length >= 1 &&
                    permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setCurrentLocationOnMap();
            } else {

            }
        } else if (requestCode == ACCESS_MY_LOCTION_REQUEST_CODE) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 400, 1000, this);
            }
        }
    }

    protected void startIntentService() {
        //check internet connection
        if (!connectionAvailable()) {
            showSnackbar();
            return;
        }

        LatLng latLng = googleMap.getCameraPosition().target;
        lastLocation = new Location("");
        lastLocation.setLatitude(latLng.latitude);
        lastLocation.setLongitude(latLng.longitude);
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, resultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, lastLocation);
        startService(intent);
    }



    private void showAddressOutput(String addressOutput) {
        searchText = addressOutput;
        edtSearch.setText(addressOutput);
    }

    protected void showSearchDropDown(int resultCode, Bundle resultData){
        //Show Search Results  ListView
        lvSearchLocation.setVisibility(View.VISIBLE);
        searchResult = resultData.getStringArray(Constants.SEARCH_RESULT_DATA_KEY);
        if (resultCode == Constants.FAILURE_RESULT) {
           showErrorDialog(searchResult[0]);
           return;
        }
        searchLatLngs = (LatLng[]) resultData.getParcelableArray(Constants.LAT_LONG_DATA_KEY);
        setSearchListViewAdapter(searchResult);
    }

    protected void showErrorDialog(String errorMessage) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(errorMessage)
                .setCancelable(true)
                .create();
        alertDialog.show();

    }

    protected void startIntentForLocationSearch() {
        String searchString = edtSearch.getText().toString();
        if (searchString.length() != 0){
            Intent intent = new Intent(this, FetchAddressIntentService.class);
            intent.putExtra(Constants.RECEIVER, resultReceiver);
            intent.putExtra(Constants.LOCATION_SEARCH_STRING, searchString);
            startService(intent);
        }

    }

    private void sendResultToActivity(String location) {
        LatLng latLng = googleMap.getCameraPosition().target;
        Intent intent = new Intent();
        intent.putExtra("lat", latLng.latitude);
        intent.putExtra("lng", latLng.longitude);
        intent.putExtra("location", location);
        setResult(101, intent);
        finish();
    }



    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        googleMap.clear();
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(searchLatLngs[position], 12));
        searchText = searchResult[position];
        edtSearch.setText(searchResult[position]);
        lvSearchLocation.setVisibility(View.GONE);
        BaseActivity.hideKeyboard(this);
    }


    class AddressResultReceiver extends ResultReceiver{

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            String addressOutput = "";

            if (resultData == null) {
                showErrorDialog(getApplicationContext().getString(R.string.noResultFound));
                return;
            }

            if(resultCode == Constants.FAILURE_RESULT) {
                showErrorDialog(resultData.getStringArray(Constants.SEARCH_RESULT_DATA_KEY)[0]);
                return;
            }

            if (resultData.getStringArray(Constants.SEARCH_RESULT_DATA_KEY) != null) {
                showSearchDropDown(resultCode, resultData);
            }else{
                // Display the address string
                // or an error message sent from the intent service.
                addressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
                if (resultCode == Constants.SUCCESS_RESULT) {
                    sendResultToActivity(addressOutput);
                }else {
                    showErrorDialog(addressOutput);
                }
            }
        }
    }
}