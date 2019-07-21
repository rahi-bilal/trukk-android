package com.trukk.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.trukk.R;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TruckImageFragment extends BaseFragment {
    private static final String TAG = "TruckImageFragment";
    View view;
    ImageView img_truck;
    String URL;

    public static TruckImageFragment newInstance(String url) {
        TruckImageFragment fragment = new TruckImageFragment();
        Bundle args = new Bundle();
        args.putString("URL", url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            URL = getArguments().getString("URL");
            Log.d(TAG, URL);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.truck_image, container, false);
        img_truck = view.findViewById(R.id.img_truck);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ImageLoadTask imageLoadTask = new ImageLoadTask(URL, img_truck);
        imageLoadTask.execute();
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
            //checking internet connection
            if (!connectionAvailable()) {
                showSnackbar();
                return null;
            }

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
            if(bitmap != null) {
                imageView.setImageBitmap(bitmap);
                imageView.setTag(url);
            }

        }
    }
}
