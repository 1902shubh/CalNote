package com.akshatrajvansh.calnote.Adapters;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.akshatrajvansh.calnote.Fragments.FragmentAttendance;
import com.akshatrajvansh.calnote.Fragments.FragmentBrowser;
import com.akshatrajvansh.calnote.Fragments.FragmentNotes;
import com.akshatrajvansh.calnote.Fragments.FragmentDeveloper;
import com.akshatrajvansh.calnote.Fragments.FragmentFriendsPay;
import com.akshatrajvansh.calnote.R;


public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_1, R.string.tab_text_2, R.string.tab_text_3, R.string.tab_text_4};
    private final Context mContext;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new FragmentAttendance();
            case 1:
                return new FragmentFriendsPay();
            case 2:
                return new FragmentNotes();
            case 3:
                return new FragmentBrowser();
            case 4:
                return new FragmentDeveloper();
        }
        return new FragmentAttendance();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        return 5;
    }
}