package com.bs.main.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bs.database.DataBaseUtil;
import com.bs.easy_chat.R;
import com.bs.main.friends.AgreeAddFriendActivity;
import com.bs.parameter.Preference;
import com.bs.util.ImageTransmissionUtil;
import com.bs.util.MyListViewAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 13273 on 2017/12/1.
 *
 */

public class AddFriendRequestAdapter extends MyListViewAdapter{

    private List<Map<String,String>> dataList;
    private Context context;
    private LinearLayout.LayoutParams lp1;
    private LinearLayout.LayoutParams lp2;

    public AddFriendRequestAdapter(Context context, List<Map<String,String>> dataList, int mRightWidth){
        super(dataList == null ? 0: dataList.size());
        this.context = context;
        this.dataList = dataList;
        lp1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        lp2 = new LinearLayout.LayoutParams(mRightWidth, LinearLayout.LayoutParams.MATCH_PARENT);
    }

    @Override
    public int getCount() {
        return dataList==null?0:dataList.size();
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        RequestViewHolder viewHolder;
        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.friend_request_item, viewGroup, false);
            viewHolder = new RequestViewHolder(view);
            view.setTag(viewHolder);
        }
        else viewHolder = (RequestViewHolder) view.getTag();

        viewHolder.itemLeft.setLayoutParams(lp1);
        viewHolder.itemRight.setLayoutParams(lp2);

        final HashMap<String,String> infoMap = (HashMap<String, String>) dataList.get(position);
        HashMap<String,String> userInfo = (HashMap<String, String>) DataBaseUtil.queryProtentialFriend(infoMap.get("user_id").equals(Preference.userInfoMap.get("user_id"))?infoMap.get("request_user"):infoMap.get("user_id")).get(0);
        viewHolder.sculpture.setImageBitmap(ImageTransmissionUtil.loadSculptureOnlyInLocal(context, userInfo.get("sculpture"),true));
        viewHolder.nickName.setText(userInfo.get("nickname"));
        if(infoMap.get("user_id").equals(Preference.userInfoMap.get("user_id"))){//自己请求添加别人为好友
            viewHolder.requestReason.setText("请求添加对方为好友");
            viewHolder.agreeBtn.setVisibility(View.GONE);
            viewHolder.state.setVisibility(View.VISIBLE);
            viewHolder.state.setText(infoMap.get("state").equals("waiting")?"等待验证":"已添加");
        }
        else{//别人请求添加自己为好友
            if(infoMap.get("state").equals("waiting")){
                viewHolder.requestReason.setText(infoMap.get("reason"));
                viewHolder.agreeBtn.setVisibility(View.VISIBLE);
                viewHolder.state.setVisibility(View.GONE);
            }
            else if(infoMap.get("state").equals("pass")){
                viewHolder.agreeBtn.setVisibility(View.GONE);
                viewHolder.state.setVisibility(View.VISIBLE);
                viewHolder.state.setText("已添加");
            }
            else{
                viewHolder.agreeBtn.setVisibility(View.GONE);
                viewHolder.state.setVisibility(View.VISIBLE);
                viewHolder.state.setText("已删除");
            }
        }

        viewHolder.agreeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("record_id", infoMap.get("record_id"));
                context.startActivity(new Intent(context, AgreeAddFriendActivity.class).putExtras(bundle));
            }
        });


        viewHolder.itemRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null)
                    mListener.onRightItemClick(v, position);
            }
        });

        return view;
    }


    /**
     * 右边点击事件监听器
     */
    private onRightItemClickListener mListener = null;

    public void setOnRightItemClickListener(onRightItemClickListener listener){
        mListener = listener;
    }

    interface onRightItemClickListener {
        void onRightItemClick(View v, int position);
    }



    private class RequestViewHolder{
        LinearLayout itemLeft;
        RelativeLayout itemRight;
        ImageView sculpture;
        TextView nickName;
        TextView requestReason;
        Button agreeBtn;
        TextView state;

        RequestViewHolder(View viewItem){
            itemLeft = viewItem.findViewById(R.id.item_left);
            itemRight = viewItem.findViewById(R.id.item_right);
            sculpture = viewItem.findViewById(R.id.sculpture);
            nickName = viewItem.findViewById(R.id.nickname);
            requestReason = viewItem.findViewById(R.id.request_reason);
            agreeBtn = viewItem.findViewById(R.id.agree_btn);
            state = viewItem.findViewById(R.id.state_view);
        }
    }
}
