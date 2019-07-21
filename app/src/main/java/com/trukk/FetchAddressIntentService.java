package com.trukk;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.trukk.utils.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.support.constraint.Constraints.TAG;

public class FetchAddressIntentService extends IntentService {

    protected ResultReceiver receiver;

    public FetchAddressIntentService() {
        super(new String(""));
    }

    @Override
    protected void onHandleIntent( Intent intent) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        if (intent == null) {
            return;
        }

        if (intent.getStringExtra(Constants.LOCATION_SEARCH_STRING) != null) {
            searchString(intent, geocoder);
        } else{
            searchLocation(intent, geocoder);
        }

    }

    private void searchLocation(Intent intent, Geocoder geocoder) {

        String errorMessage= "";
        List<Address> addresses = null;

        //Get location passed to it through extra
        Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);
        receiver = intent.getParcelableExtra(Constants.RECEIVER);


        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

        }catch (IOException ioException) {
            errorMessage = getString(R.string.service_not_available);
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = getString(R.string.invalid_lat_long_used);
            Log.e(TAG, errorMessage + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " +
                    location.getLongitude(), illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = getString(R.string.no_address_found);
                Log.e(TAG, errorMessage);
            }
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
        }else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            Log.i(TAG, getString(R.string.address_found));
            deliverResultToReceiver(Constants.SUCCESS_RESULT,
                    TextUtils.join(System.getProperty("line.separator"),
                            addressFragments));
        }
    }

    private void searchString(Intent intent, Geocoder geocoder) {
        String errorMessage= "";
        List<Address> addresses = null;

        //Get Location Search String
        String search = intent.getStringExtra(Constants.LOCATION_SEARCH_STRING);
        receiver = intent.getParcelableExtra(Constants.RECEIVER);
        //try to get location result from string
        try {
            addresses = geocoder.getFromLocationName(search, 6);
        } catch (IOException e) {
            errorMessage = getString(R.string.service_not_available);
        }

        //Handle cases where no address found
        if (addresses == null || addresses.size() == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = getString(R.string.no_address_found);
                Log.e(TAG, errorMessage);
            }
            deliverStringSearchResultToReceiver(Constants.FAILURE_RESULT,null, new String[]{errorMessage});
        } else {
            String[] addressResult = new String[addresses.size()];
            for (int i=0; i<addresses.size(); i++) {
                Address address = addresses.get(i);
                ArrayList<String> addressFragments = new ArrayList<String>();

                for(int j = 0; j <= address.getMaxAddressLineIndex(); j++) {
                    addressFragments.add(address.getAddressLine(j));
                }
                addressResult[i] = TextUtils.join(System.getProperty("line.separator"), addressFragments);
            }
            deliverStringSearchResultToReceiver(Constants.SUCCESS_RESULT, addresses, addressResult);

        }



    }

    private void deliverStringSearchResultToReceiver(int resultCode, List<Address> addresses, String[] searchResult) {
        Bundle bundle = new Bundle();
        bundle.putStringArray(Constants.SEARCH_RESULT_DATA_KEY, searchResult);
        if (addresses != null){
            LatLng[] latLngs = new LatLng[addresses.size()];
            for (int i = 0; i < addresses.size(); i++){
                latLngs[i] = new LatLng(addresses.get(i).getLatitude(), addresses.get(i).getLongitude());
                bundle.putParcelableArray(Constants.LAT_LONG_DATA_KEY, latLngs);
            }
            receiver.send(resultCode, bundle);
        }
    }


    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        receiver.send(resultCode, bundle);
    }
}
