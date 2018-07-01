package com.bs.function.notes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bs.easy_chat.R;
import com.bs.tool_package.TimeTools;
import com.bs.util.MyListViewAdapter;

import java.util.List;
import java.util.Map;

/**
 * Created by 13273 on 2017/10/1.
 *
 */

class NotesAdapter extends MyListViewAdapter {

    private List<Map<String,String>> mMessageItems;
    private Context context;

    private LinearLayout.LayoutParams lp1;

    private LinearLayout.LayoutParams lp2;


    NotesAdapter(Context context,List<Map<String ,String>> mMessageItems, int mRightWidth){
        super(mMessageItems.size());
        this.mMessageItems=mMessageItems;
        this.context = context;

        lp1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        lp2 = new LinearLayout.LayoutParams(mRightWidth, LinearLayout.LayoutParams.MATCH_PARENT);
    }


    @Override
    public int getCount() {return mMessageItems.size();}

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.notes_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }
        else
            holder = (ViewHolder) convertView.getTag();


        holder.item_left.setLayoutParams(lp1);
        holder.item_right.setLayoutParams(lp2);

        String content = mMessageItems.get(position).get("content");
        holder.title.setText(content.length()>15 ?content.substring(0,15):content);

        String dateStr = mMessageItems.get(position).get("date");
        holder.date.setText((dateStr.equals(TimeTools.generateContentFormatTime()))?mMessageItems.get(position).get("detail_date").substring(13,18):dateStr);

        holder.item_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onRightItemClick(v, position);
                }
            }
        });

        return convertView;
    }

   private class ViewHolder {
    LinearLayout item_left;
    RelativeLayout item_right;

    TextView title;
    TextView date;
    TextView item_right_txt;

        ViewHolder(View view) {
            title = view.findViewById(R.id.title);
            date = view.findViewById(R.id.date);
            item_right_txt =view.findViewById(R.id.item_right_txt);
            item_left = view.findViewById(R.id.item_left);
            item_right = view.findViewById(R.id.item_right);
        }
    }



    /**
     * 单击事件监听器
     */
    private onRightItemClickListener mListener = null;

    void setOnRightItemClickListener(onRightItemClickListener listener){
        mListener = listener;
    }

    interface onRightItemClickListener {
        void onRightItemClick(View v, int position);
    }
}
