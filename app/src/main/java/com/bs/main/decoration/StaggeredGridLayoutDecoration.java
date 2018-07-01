package com.bs.main.decoration;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by 13273 on 2017/10/28.
 *
 */

public class StaggeredGridLayoutDecoration extends RecyclerView.ItemDecoration {

    private int space;

    public StaggeredGridLayoutDecoration(int space){
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left = space;
        outRect.right = space;
        outRect.bottom = space;
        if(parent.getChildAdapterPosition(view) == 0){
            outRect.top = space;
        }
    }
}
