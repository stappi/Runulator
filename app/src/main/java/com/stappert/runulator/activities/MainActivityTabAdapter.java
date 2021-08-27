package com.stappert.runulator.activities;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.stappert.runulator.R;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class MainActivityTabAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_run, R.string.tab_forecast, R.string.tab_fitness};

    /**
     * Tab run.
     */
    private final TabRun tabRun = new TabRun();

    private final Context mContext;

    public MainActivityTabAdapter(@NonNull FragmentManager fm, int behavior, Context mContext) {
        super(fm, behavior);
        this.mContext = mContext;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return tabRun;
            case 1:
                return new TabForecast();
            case 2:
                return new TabHealth();
        }
        return null;
    }

    public TabRun getTabRun() {
        return tabRun;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 3;
    }
}