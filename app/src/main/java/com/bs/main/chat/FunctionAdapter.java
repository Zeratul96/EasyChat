package com.bs.main.chat;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bs.easy_chat.R;
import com.bs.util.MyListViewAdapter;

/**
 * Created by 13273 on 2017/11/14.
 *
 */

class FunctionAdapter extends MyListViewAdapter{

    private Context context;

    private int[] functionItems = new int[]{ R.drawable.express, R.drawable.picture, R.drawable.take_photo};

    private String[] label = new String[]{"表情","照片","拍摄"};

    FunctionAdapter(Context context){
        super(3);
        this.context = context;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null)
            view = LayoutInflater.from(context).inflate(R.layout.chat_function_item, viewGroup, false);

        ((TextView)view.findViewById(R.id.label)).setText(label[i]);
        ((ImageView)view.findViewById(R.id.icon)).setImageDrawable(ContextCompat.getDrawable(context, functionItems[i]));

        return view;
    }
}
