package com.bs.function.diary;

import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bs.easy_chat.R;
import com.bs.tool_package.TimeTools;
import com.bs.util.MyListViewAdapter;
import com.bs.widget.MyGridView;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 13273 on 2017/10/13.
 *
 */

public class TimeStyleAdapter extends MyListViewAdapter {

    private DiaryActivity activity;
    private List<String> dateShower = new ArrayList<>();

    TimeStyleAdapter(int size, DiaryActivity activity){
        super(size);
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return activity.dataList.size();
    }

    @Override
    public View getView(final int i, View convertView, ViewGroup viewGroup) {

        TimeStyleAdapter.ViewHolder viewHolder;
        if(convertView == null){
            convertView = LayoutInflater.from(activity).inflate(R.layout.timeline_item, viewGroup, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        else
            viewHolder = (ViewHolder) convertView.getTag();

        if(i != 0)
            viewHolder.titlePadding.setVisibility(View.GONE);
        else
            viewHolder.titlePadding.setVisibility(View.VISIBLE);

        HashMap<String,String> map = (HashMap<String, String>) activity.dataList.get(i);
        viewHolder.title.setText(map.get("title"));
        if(map.get("title").length()>0){
            viewHolder.title.setVisibility(View.VISIBLE);
        }
        else{
            viewHolder.title.setVisibility(View.GONE);
        }


        String content = map.get("content");
        if(content.length()>0){
            viewHolder.content.setVisibility(View.VISIBLE);
            viewHolder.content.setText(content.length()>50?content.substring(0,50)+"...":content);
        }
        else{
            viewHolder.content.setVisibility(View.GONE);
        }

        if(dateShower.get(i).contains("#")){
            String[] s = dateShower.get(i).split("#");
            viewHolder.date.setText(s[1]);
            viewHolder.year.setText(s[0]);
        }else{
            viewHolder.date.setText(dateShower.get(i));
            viewHolder.year.setText("");
        }
        viewHolder.dot.setImageDrawable
                (dateShower.get(i).equals("") ? ContextCompat.getDrawable(activity, R.drawable.time_node):ContextCompat.getDrawable(activity, R.drawable.time_node_main));

        viewHolder.detailTime.setText(map.get("detail_date").substring(13, 18));


        String[] picturePaths = map.get("pictures").split("<#>");
        if(picturePaths.length > 0 && !picturePaths[0].equals("")){
            viewHolder.gridView.setVisibility(View.VISIBLE);
            viewHolder.gridView.setAdapter(new TimeStyleAdapter.GridViewAdapter(picturePaths));
        }
        else
            viewHolder.gridView.setVisibility(View.GONE);



/*        viewHolder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDiary(i);
                activity.dataList.remove(i);
                dateConvert();
                TimeStyleAdapter.this.notifyDataSetChanged();
            }
        });

        if(map.get("state").equals("0"))
            viewHolder.deployBtn.setEnabled(false);
        else{
            viewHolder.deployBtn.setEnabled(true);
            viewHolder.deployBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                }
            });
        }*/
        return convertView;
    }


/*    private void deleteDiary(int i){
        String sql = "delete from diary where primary_key = '"+activity.dataList.get(i).get("primary_key")+"'";
        DataBaseUtil.delete(sql, SQLLiteConstant.NOTES_TABLE);
    }*/

    void dateConvert() {
        String presentDate = "";
        for (Map<String, String> map : activity.dataList)
        {
            String date = map.get("date");
            String nowDate = TimeTools.generateContentFormatTime();
            //如果格式和今天一样那么赋值为今天
            if (date.equals(nowDate))
                date = "今天";
            //如果不是今天那么进行年份判断
            if (!date.equals("今天")){
                //如果是今年
                if(nowDate.substring(0,4).equals(date.substring(0,4))){
                    date = date.substring(5, date.length());
                }
                else {
                    StringBuilder stringBuffer = new StringBuilder(date);
                    stringBuffer.replace(4,5,"#");
                    date = stringBuffer.toString();
                }
            }
            //如果和现在展示的时间一样那么不显示（同一天中不是第一条日记）
            if (date.equals(presentDate)) {
                date = "";
            } else {
                presentDate = date;
            }

            dateShower.add(date);
        }
    }


    private class GridViewAdapter extends MyListViewAdapter{

        String[] path;

        GridViewAdapter(String[] path){
            super(path.length>4 ? 4 : path.length);
            this.path = path;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            CreateDiaryActivity.ViewHolder holder;

            if(view == null){
                view = LayoutInflater.from(activity).inflate(R.layout.picture_item, viewGroup, false);
                holder = new CreateDiaryActivity.ViewHolder((ImageView) view.findViewById(R.id.picture));
                view.setTag(holder);
            }
            else{
                holder = (CreateDiaryActivity.ViewHolder) view.getTag();
            }

            if(((MyGridView)viewGroup).isOnMeasure)
                return view;

            Glide.with(activity).
                    load(new File(path[i])).
                    centerCrop().
                    into(holder.imageView);
            return view;
        }
    }

    private class ViewHolder{

        View titlePadding;
        TextView title;
        TextView content;
        TextView date;
        TextView year;
        TextView detailTime;
        GridView gridView;

        ImageView dot;

        ViewHolder(View view){
            titlePadding = view.findViewById(R.id.title_padding);
            title = view.findViewById(R.id.title);
            content = view.findViewById(R.id.content);
            date = view.findViewById(R.id.date);
            year = view.findViewById(R.id.year);
            detailTime = view.findViewById(R.id.detail_time);
            gridView = view.findViewById(R.id.grid_view);
            dot = view.findViewById(R.id.dot);
        }

    }

}
