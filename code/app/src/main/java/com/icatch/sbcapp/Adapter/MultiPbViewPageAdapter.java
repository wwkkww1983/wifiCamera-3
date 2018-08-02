package com.icatch.sbcapp.Adapter;



import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * Created by b.jiang on 2016/1/5.
 */
public class MultiPbViewPageAdapter extends FragmentPagerAdapter {
    ArrayList<Fragment> list;

    public MultiPbViewPageAdapter(FragmentManager fm,ArrayList<Fragment> list){
        super(fm);
        this.list = list;
    }

    @Override
    public Fragment getItem(int position) {
        return list.get(position);
    }

    @Override
    public int getCount() {
        return list.size();
    }
}
