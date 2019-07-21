package com.trukk.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.trukk.fragments.TruckImageFragment;

import java.util.ArrayList;

public class ViewTruckPagerAdapter extends FragmentPagerAdapter {

    private ArrayList<String> urls;

    public ViewTruckPagerAdapter(FragmentManager fragmentManager, ArrayList<String> urls) {
        super(fragmentManager);
        this.urls = urls;
    }

    @Override
    public Fragment getItem(int position) {
        return TruckImageFragment.newInstance(urls.get(position));
    }

    @Override
    public int getCount() {
        return urls.size();
    }
}
