package com.bs.function.moments;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bs.database.DataBaseUtil;
import com.bs.easy_chat.R;
import com.bs.tool_package.TimeTools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 13273 on 2017/11/4.
 *
 */

class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.ViewHolder>{

    private Context context;
    private List<Map<String,String>> data;
    private List<Map<String,String>> userInfoList;
    private OnTouchContentListener listener;

     MsgAdapter(Context context, List<Map<String,String>> data){
        this.context = context;
        this.data = data;
        userInfoList = DataBaseUtil.queryFriends();
    }

    public MsgAdapter setData(List<Map<String,String>> data){
        this.data = data;
        userInfoList = DataBaseUtil.queryFriends();
        return this;
    }

    @Override
    public int getItemCount() {return data==null?0:data.size();}

    @Override
    public MsgAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.msg_item, parent, false));
    }

    @Override
    public void onBindViewHolder(MsgAdapter.ViewHolder holder, int position) {
        final HashMap<String,String> hashMap = (HashMap<String, String>) data.get(position);

        String name = hashMap.get("user_id");
        Map<String, String> userInfo = findUserInfo(hashMap.get("user_id"));
        if(userInfo != null)
            name = userInfo.get("remark_name").length() > 0 ? userInfo.get("remark_name") : userInfo.get("nickname");

        holder.callBackInfo.setText(name+"  回复了你：");

        holder.content.setText(hashMap.get("content"));
        holder.detailTime.setText(TimeTools.parseDetailTime(hashMap.get("detail_time")));

        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null != listener)
                    listener.onTouchContent(hashMap.get("moments_id"), hashMap.get("author_id"), hashMap.get("comment_id"));
            }
        });
    }

    private Map<String,String> findUserInfo(@NonNull String userID){
        for (Map<String,String> map : userInfoList)
            if(map.get("friend_id").equals(userID))
                return map;
        return null;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView detailTime;
        TextView callBackInfo;
        TextView content;
        LinearLayout item;

        ViewHolder(View view){
            super(view);
            item = view.findViewById(R.id.item_layout);
            detailTime = view.findViewById(R.id.detail_time);
            callBackInfo = view.findViewById(R.id.call_back_info);
            content = view.findViewById(R.id.call_back_content);
        }
    }

    /**
     * 触摸回复内容回调接口
     */
   interface OnTouchContentListener{
        void onTouchContent(String momentsID, String authorID, String commentID);
    }

    void setOnTouchContentListener(OnTouchContentListener l){
        listener = l;
    }
}
