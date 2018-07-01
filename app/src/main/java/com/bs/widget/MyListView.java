package com.bs.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.ListView;

/**
 * Created by 13273 on 2017/9/23.
 *
 *触摸之后的变色问题以及触摸的Item分割线的显示与隐藏
 */

public class MyListView extends ListView{

    private int originY;

    private int originPosition;

    final int DEVIATION = 10;

    public MyListView(Context context) {
        super(context);
    }

    public MyListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        int position = pointToPosition(x, y);

        // 由于pointToPosition返回的是ListView所有item中被点击的item的position，
        // 而listView只会缓存可见的item，因此getChildAt()的时候，需要通过减去getFirstVisiblePosition()
        // 来计算被点击的item在可见items中的位置。
        LinearLayout row = (LinearLayout) getChildAt(position - getFirstVisiblePosition());

        switch (ev.getAction())
        {
            case MotionEvent.ACTION_DOWN:

                if (position != INVALID_POSITION)
                {
                    originY = y;
                    originPosition = position;
                    row.setBackgroundColor(Color.parseColor("#d9d9d9"));
                    //去除灰色的线
                    if(position > 0 && position - 1 - getFirstVisiblePosition() >= 0){
                        LinearLayout lastRow = (LinearLayout) getChildAt(position - 1 - getFirstVisiblePosition());
                        lastRow.getChildAt(1).setBackgroundColor(Color.parseColor("#FFFFFF"));
                    }
                }

                break;

            case MotionEvent.ACTION_UP:

            case MotionEvent.ACTION_MOVE:
                LinearLayout originRow = (LinearLayout) getChildAt(originPosition - getFirstVisiblePosition());
                if(Math.abs( y - originY ) > DEVIATION || originRow.getTop()> y || originRow.getBottom() < y )
                {
                    originRow.setBackgroundColor(Color.parseColor("#FFFFFF"));

                    //恢复灰色的线
                    if(originPosition > 0 && originPosition - 1 - getFirstVisiblePosition() >= 0){
                        LinearLayout lastRow = (LinearLayout) getChildAt(originPosition - 1 - getFirstVisiblePosition());
                        lastRow.getChildAt(1).setBackgroundColor(Color.parseColor("#d9d9d9"));
                    }

                    return true;
                }

                break;
        }
        return super.onTouchEvent(ev);
    }
}
