package com.trukk;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.trukk.fragments.ContactUsFragment;
import com.trukk.fragments.HistoryFragment;
import com.trukk.fragments.MyAccountFragment;
import com.trukk.fragments.ShipAnythingFragment;
import com.trukk.fragments.ShipmentFragment;
import com.trukk.interfaces.OnFragmentInteractionListener;
import com.yalantis.ucrop.UCrop;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends BaseActivity
        implements OnFragmentInteractionListener, View.OnClickListener{
    public static final String SHIP_ANYTHING_FRAGMENT = "ship_anything_fragment";
    public static final String SHIPMENT_FRAGMENT = "shipment_fragment";
    public final static String CONTACT_US_FRAGMENT = "contact_us_fragment";
    public final static String HISTORY_FRAGMENT = "history_fragment";
    public final static String MY_ACCOUNT_FRAGMENT = "my_account_fragment";
    public final static String TAG = "MainActivity";


    public static String CURRENT_FRAGMENT;

    private FragmentManager fragmentManager;


    private ImageButton btnMenuToolbar;
    private TextView tvNavHome;
    private TextView tvNavMyAccount;
    private TextView tvNavHistory;
    private TextView tvNavContactUs;
    private TextView tvNavLogout;
    private TextView tvNavUsername;
    private ImageView img_nav_profile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewsById();

        setOnClickListeners();

        //set Username
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                String fullname = getResources().getString(R.string.full_name,
                        getSharedPref(getBaseContext(), USER_FNAME), getSharedPref(getBaseContext(), USER_LNAME));
                if (fullname != null) {
                    tvNavUsername.setText(fullname);
                }
                String imageUrl = getSharedPref(getBaseContext(), USER_IMAGE);
                if (imageUrl == null){
                    return;
                }
                if (img_nav_profile.getTag()!=null) {
                    if (imageUrl.equals(img_nav_profile.getTag().toString()))
                    return;
                }
                ImageLoadTask imageLoadTask = new ImageLoadTask(imageUrl, img_nav_profile);
                imageLoadTask.execute();

            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();



        //Adding ShipAnythingFragment
        fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack(SHIP_ANYTHING_FRAGMENT, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.lyt_scroll_view, ShipAnythingFragment.newInstance(null, null))
                .commit();


    }


    private void findViewsById() {
        //Toolbar Menu ImageButton
        btnMenuToolbar = findViewById(R.id.btn_menu_toolbar);

        //Nav Drawer Views
        tvNavHome = findViewById(R.id.tv_nav_home);
        tvNavMyAccount = findViewById(R.id.tv_nav_my_account);
        tvNavHistory = findViewById(R.id.tv_nav_history);
        tvNavContactUs = findViewById(R.id.tv_nav_contact_us);
        tvNavLogout = findViewById(R.id.tv_nav_logout);
        tvNavUsername = findViewById(R.id.tv_nav_profile_name);
        img_nav_profile = findViewById(R.id.img_nav_profile);
    }

    private void setOnClickListeners() {
        //Toolbar menu ImageButton
        btnMenuToolbar.setOnClickListener(this);

        //Nav Drawer Views
        tvNavHome.setOnClickListener(this);
        tvNavMyAccount.setOnClickListener(this);
        tvNavHistory.setOnClickListener(this);
        tvNavContactUs.setOnClickListener(this);
        tvNavLogout.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_menu_toolbar:     onMenuButtonClick();
                                            break;
            case R.id.tv_nav_home:          replaceFragment(ShipAnythingFragment.newInstance(null, null), SHIP_ANYTHING_FRAGMENT);
                                            break;
            case R.id.tv_nav_my_account:    replaceFragment(MyAccountFragment.newInstance(null, null), MY_ACCOUNT_FRAGMENT);
                                            break;
            case R.id.tv_nav_history:       replaceFragment(HistoryFragment.newInstance(null, null), HISTORY_FRAGMENT);
                                            break;
            case R.id.tv_nav_contact_us:    replaceFragment(ContactUsFragment.newInstance(null, null), CONTACT_US_FRAGMENT);
                                            break;
            case R.id.tv_nav_logout:        logout();
        }
    }

    private void onMenuButtonClick() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            drawer.openDrawer(GravityCompat.START);
        }
    }

    private void closeDrawer() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

    }

    private void replaceFragment(Fragment fragment, String newFragment) {
        if (CURRENT_FRAGMENT != newFragment) {
            fragmentManager.popBackStack(newFragment, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentManager
                    .beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                    .replace(R.id.lyt_scroll_view, fragment)
                    .addToBackStack(CURRENT_FRAGMENT)
                    .commit();
            CURRENT_FRAGMENT = newFragment;
        }
        closeDrawer();
    }

    private void logout() {
        resetSharedPref(getBaseContext());
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void updateCurrentFragment(String fragmentName) {
        CURRENT_FRAGMENT = fragmentName;
    }


    @Override
    public void shipAnythingGo() {

        replaceFragment(ShipmentFragment.newInstance(null, null), SHIPMENT_FRAGMENT);
    }

    @Override
    public void onShipmentComplete() {
        replaceFragment(ShipAnythingFragment.newInstance(null, null), SHIP_ANYTHING_FRAGMENT);
    }

    @Override
    public void updateProfile() {

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
