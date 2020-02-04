package com.akshatrajvansh.calnote.Adapters;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.akshatrajvansh.calnote.Fragments.FragmentAttendance;
import com.akshatrajvansh.calnote.Fragments.FragmentUdhari;
import com.akshatrajvansh.calnote.R;


public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_1, R.string.tab_text_2};
    private final Context mContext;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                FragmentAttendance fragmentAttendance = new FragmentAttendance();
                return fragmentAttendance;
            case 1:
                FragmentUdhari fragmentUdhari = new FragmentUdhari();
                return fragmentUdhari;
            default:
                FragmentAttendance fragmentDefault = new FragmentAttendance();
                return fragmentDefault;
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 2;
    }
}