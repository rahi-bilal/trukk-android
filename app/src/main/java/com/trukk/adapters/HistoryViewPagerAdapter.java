package com.trukk.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.trukk.fragments.CurrentHistoryFragment;

public class HistoryViewPagerAdapter extends FragmentStatePagerAdapter {

    public HistoryViewPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);

    }



    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:     return CurrentHistoryFragment.newInstance("current", null);
            case 1:     return CurrentHistoryFragment.newInstance("past", null);
            default:    return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
