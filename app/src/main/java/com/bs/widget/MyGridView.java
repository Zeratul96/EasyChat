package com.bs.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * Created by 13273 on 2017/10/4.
 *解决GridView多次调用Adapter问题
 */

public class MyGridView extends GridView{

    public boolean isOnMeasure;

    public MyGridView(Context context){
        super(context);
    }

    public MyGridView(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    public MyGridView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        isOnMeasure = true;
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        isOnMeasure = false;
        super.onLayout(changed, l, t, r, b);
    }
}
