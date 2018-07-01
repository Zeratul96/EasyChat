package com.bs.util;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Created by 13273 on 2017/7/30.
 *
 */

public abstract class MyListViewAdapter extends BaseAdapter{

    private int count;

    protected MyListViewAdapter(int count){this.count = count;}

    @Override
    public int getCount() {return count;}

    @Override
    public int getViewTypeCount(){return 1;}

    @Override
    public int getItemViewType(int position){return 0;}

    @Override
    public Object getItem(int i) {return null;}

    @Override
    public long getItemId(int i) {return 0;}

    @Override
    public abstract View getView(int i, View view, ViewGroup viewGroup);
}
