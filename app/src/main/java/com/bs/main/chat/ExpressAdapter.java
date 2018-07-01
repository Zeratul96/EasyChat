package com.bs.main.chat;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bs.easy_chat.R;
import com.bs.util.MyListViewAdapter;

/**
 * Created by 13273 on 2017/10/26.
 *
 */

class ExpressAdapter extends MyListViewAdapter{

    private Context context;

    private int[] expressNum = new int[]
    {
        R.drawable.emoji_1,R.drawable.emoji_2,R.drawable.emoji_3,R.drawable.emoji_4,R.drawable.emoji_5,R.drawable.emoji_6,
        R.drawable.emoji_7,R.drawable.emoji_8,R.drawable.emoji_9,R.drawable.emoji_10,R.drawable.emoji_11,R.drawable.emoji_12,
        R.drawable.emoji_13,R.drawable.emoji_14,R.drawable.emoji_15,R.drawable.emoji_16,R.drawable.emoji_17,R.drawable.emoji_18,
        R.drawable.emoji_19,R.drawable.emoji_20,R.drawable.emoji_21,R.drawable.emoji_22,R.drawable.emoji_23,R.drawable.emoji_24,
        R.drawable.emoji_25,R.drawable.emoji_26,R.drawable.emoji_27,R.drawable.emoji_28,R.drawable.emoji_29,R.drawable.emoji_30
    };

    ExpressAdapter(Context context){
        super(30);
        this.context = context;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.picture_item, viewGroup, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }
        else
            holder = (ViewHolder) view.getTag();

        holder.expressImage.setImageDrawable(ContextCompat.getDrawable(context, expressNum[i]));

        return view;
    }

    private class ViewHolder{

        ImageView expressImage;
        ViewHolder(View view){
            this.expressImage = view.findViewById(R.id.picture);
        }
    }
}
