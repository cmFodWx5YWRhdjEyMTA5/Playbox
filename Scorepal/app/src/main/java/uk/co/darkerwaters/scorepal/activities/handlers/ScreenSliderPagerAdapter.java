package uk.co.darkerwaters.scorepal.activities.handlers;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class ScreenSliderPagerAdapter extends FragmentStatePagerAdapter {

    private final Fragment[] pages;

    public ScreenSliderPagerAdapter(FragmentManager fm, Fragment[] pages) {
        super(fm);
        this.pages = pages;
    }

    @Override
    public Fragment getItem(int position) {
        return this.pages[position];
    }

    @Override
    public int getCount() {
        return this.pages.length;
    }


}
