package com.pengcit.jorfelag.pengolahancitra;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.pengcit.jorfelag.pengolahancitra.contrast_enhancement.ContrastEnhancementFragment;
import com.pengcit.jorfelag.pengolahancitra.histogram.ShowHistogramFragment;

public class TabPagerAdapter extends FragmentPagerAdapter {

    int tabCount;

    public TabPagerAdapter(FragmentManager fm, int numberOfTabs) {
        super(fm);
        this.tabCount = numberOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return new LoadImageFragment();
            case 1:
                return new ShowHistogramFragment();
            case 2:
                return new ContrastEnhancementFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }
}
