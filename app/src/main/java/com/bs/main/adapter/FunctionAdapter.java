package com.bs.main.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bs.easy_chat.R;
import com.bs.util.MyListViewAdapter;
import com.bs.widget.MyGridView;

/**
 * Created by 13273 on 2017/10/30.
 *
 */

public class FunctionAdapter extends MyListViewAdapter{

    private String[] label;
    private Context context;
    private int[] icon;

    public FunctionAdapter(String[] label,int[] icon ,Context context){
        super(label.length);
        this.label = label;
        this.context = context;
        this.icon = icon;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null)
            view = LayoutInflater.from(context).inflate(R.layout.function_item, viewGroup, false);

        if(((MyGridView)viewGroup).isOnMeasure) return view;

        ((TextView)view.findViewById(R.id.label)).setText(label[i]);
        ((ImageView)view.findViewById(R.id.icon)).setImageDrawable(ContextCompat.getDrawable(context, icon[i]));

        return view;
    }
}
