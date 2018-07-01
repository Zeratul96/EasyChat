package com.bs.function;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bs.easy_chat.R;

/**
 * Created by 13273 on 2017/9/30.
 *
 */

class WebListViewHolder {

    ImageView icon;
    TextView textView;

    WebListViewHolder(View view){
        textView = view.findViewById(R.id.title_view);
        icon = view.findViewById(R.id.Image);
    }
}
