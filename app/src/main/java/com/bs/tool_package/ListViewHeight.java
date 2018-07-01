package com.bs.tool_package;

import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Created by 13273 on 2017/7/28.
 *
 */

public class ListViewHeight {

    /**
     * scrollview与ListView合用会出现listView只显示一行多点。此方法是为了定死ListView的高度就不会出现以上状况
     * 算出ListView的高度
     */
    public static void setListViewHeight(ListView listView)
    {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1)) + listView.getPaddingTop() + listView.getPaddingBottom();
        listView.setLayoutParams(params);
    }
}
