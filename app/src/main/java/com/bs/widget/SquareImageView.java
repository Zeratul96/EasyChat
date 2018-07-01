package com.bs.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by 13273 on 2017/10/4.
 *
 */

public class SquareImageView extends ImageView {

    public SquareImageView(Context context){
        super(context);
    }

    public SquareImageView(Context context, AttributeSet attrs){
        super(context,attrs);
    }

    public SquareImageView(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
