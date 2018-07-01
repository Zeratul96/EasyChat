package com.bs.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by 13273 on 2017/10/9.
 *将ListView完全展开配合ScrollView
 * 嵌套使用要添加:
 * sv = (ScrollView) findViewById(R.id.act_solution_4_sv);
 sv.smoothScrollTo(0, 0);
 *
 */

public class MyListView2 extends ListView{

    public MyListView2(Context context) {
        super(context);
    }

    public MyListView2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyListView2(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
