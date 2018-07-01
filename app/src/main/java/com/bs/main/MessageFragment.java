package com.bs.main;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bs.database.DataBaseUtil;
import com.bs.easy_chat.R;
import com.bs.main.adapter.MessageAdapter;
import com.bs.main.chat.ChatActivity;
import com.bs.parameter.Preference;
import com.bs.parameter.SQLLiteConstant;
import com.bs.tool_package.TimeTools;
import com.bs.widget.SwipeListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 13273 on 2017/9/16.
 *
 */

public class MessageFragment extends Fragment{

    AnimationDrawable spinner;
    ImageView spinningImageView;
    TextView connectionState;
    SwipeListView swipeListView;
    MessageAdapter adapter;
    List<Map<String,String>> messageList = new ArrayList<>();
    Map<String ,List<Map<String,String>>> messageDetail = new HashMap<>();
    Map<String, Integer> contactWith = new HashMap<>();//未读消息的条数

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.message_layout, container, false);

        spinningImageView = view.findViewById(R.id.spinnerImageView);
        connectionState = view.findViewById(R.id.title_name);
        spinner = (AnimationDrawable) spinningImageView.getBackground();
        spinner.start();

        setSpinnerState(Preference.messageSpinning);

        swipeListView = view.findViewById(R.id.message_view);
        swipeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, String> infoMap = messageList.get(position);
                Map<String,String> friendMap = DataBaseUtil.queryFriendsExactly(infoMap.get("contact_with")).get(0);
                Bundle bundle = new Bundle();
                bundle.putString("name", friendMap.get("remark_name").length()==0?friendMap.get("nickname"):friendMap.get("remark_name"));
                bundle.putString("back_type", "易聊");
                bundle.putString("friend_id", infoMap.get("contact_with"));
                bundle.putString("sculpture", friendMap.get("sculpture"));

                clearUnreadNumber(infoMap.get("contact_with"));
                ArrayList<Map<String,String>> message = (ArrayList<Map<String,String>>) messageDetail.get(infoMap.get("contact_with"));
                startActivity(new Intent(MainActivity.mainActivity, ChatActivity.class).putExtras(bundle).putExtra("message_detail", message));
            }
        });
        pickUpNecessaryInfoAndRefreshUI(DataBaseUtil.queryMessage());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        swipeListView.setAdapter(adapter = new MessageAdapter(messageList, contactWith, MainActivity.mainActivity, swipeListView.getRightViewWidth()));
        adapter.setSwipeListView(swipeListView);
        adapter.setOnRightItemClickListener(new MessageAdapter.onRightItemClickListener() {
            @Override
            public void onRightItemClick(View v, int position) {
                adapter.showAlertDialog(messageList.get(position));
            }
        });
    }

    /**
     * 改变title的状态
     * @param isSpinning:正在旋转
     */
    void setSpinnerState(boolean isSpinning){
        if(isSpinning){
            spinningImageView.setVisibility(View.VISIBLE);
            spinner.start();
            connectionState.setText("连接中...");
        }
        else{
            if(spinner.isRunning()) spinner.stop();
            spinningImageView.setVisibility(View.GONE);
            connectionState.setText("易聊");
        }
    }

    /**
     * 整理所有的聊天数据
     * @param mData：原始数据
     */
    void pickUpNecessaryInfoAndRefreshUI(List<Map<String,String>> mData){
        messageList.clear();
        messageDetail.clear();
        contactWith.clear();
        for(Map<String,String> perData:mData)
        {
            Map<String,String> titleMap = new HashMap<>();
            titleMap.put("contact_with", perData.get("send_user").equals(Preference.userInfoMap.get("user_id"))?perData.get("receive_user"):perData.get("send_user"));//确定和谁聊天
            if(!contactWith.keySet().contains(titleMap.get("contact_with"))){//如果这个人还没有在消息列表里存在则添加并且载入以下内容
                titleMap.put("last_time", TimeTools.parseMessageTime(perData.get("time")));//确定聊天时间
                titleMap.put("last_type", perData.get("type"));//确定最后一次的聊天类型
                titleMap.put("last_content_brief", perData.get("content"));//确定最后一次聊天大体内容
                messageList.add(titleMap);//添加进消息列表
                messageDetail.put(titleMap.get("contact_with"), new ArrayList<Map<String, String>>());//在详细消息列表里占位
                contactWith.put(titleMap.get("contact_with"), 0);//加入消息列表中已存在的联系人
            }

            if(perData.get("is_send").equals("0") && !perData.get("send_user").equals(Preference.userInfoMap.get("user_id"))){
                Integer num = contactWith.get(titleMap.get("contact_with"));
                contactWith.put(titleMap.get("contact_with"), ++num);
            }

            messageDetail.get(titleMap.get("contact_with")).add(0,perData);//将此条记录添加进详细聊天记录表（因为数据库里搜出来记录是按时间后先排序 这里从后往前塞变更排序为时间先后）
        }
        if(adapter != null)
            adapter.updateFriendInfo().notifyDataSetChanged();
    }

    /**
     *收到单条记录
     * @param perData:消息信息
     */
    public void onReceiveSingleMessage(Map<String,String> perData, boolean isNeedBadge){
        Map<String,String> titleMap = new HashMap<>();
        titleMap.put("contact_with", perData.get("send_user").equals(Preference.userInfoMap.get("user_id"))?perData.get("receive_user"):perData.get("send_user"));//确定和谁聊天
        if(!contactWith.keySet().contains(titleMap.get("contact_with"))){//如果这个人还没有在消息列表里存在则添加并且载入以下内容
            messageList.add(0,titleMap);//添加进消息列表最顶端
            messageDetail.put(titleMap.get("contact_with"), new ArrayList<Map<String, String>>());//在详细消息列表里占位
            contactWith.put(titleMap.get("contact_with"), isNeedBadge ? 1:0);//加入消息列表中已存在的联系人
        }
        else{//最近消息列表已经包含这个联系人
            for(Map<String,String> perMap:messageList){
                if(perMap.get("contact_with").equals(titleMap.get("contact_with"))){
                    titleMap = perMap;
                    break;
                }
            }
            messageList.remove(titleMap);
            messageList.add(0, titleMap);
            if(isNeedBadge)
                contactWith.put(titleMap.get("contact_with"), contactWith.get(titleMap.get("contact_with"))+1);
        }
        titleMap.put("last_time", TimeTools.parseMessageTime(perData.get("time")));//确定聊天时间
        titleMap.put("last_type", perData.get("type"));//确定最后一次的聊天类型
        titleMap.put("last_content_brief", perData.get("content"));//确定最后一次聊天大体内容

        if(messageDetail.get(titleMap.get("contact_with")) != null)
        messageDetail.get(titleMap.get("contact_with")).add(perData);//将此条记录添加进详细聊天记录表

        if(adapter != null)
            adapter.updateFriendInfo().notifyDataSetChanged();
    }

    public void clearUnreadNumber(String friendID){
        contactWith.put(friendID, 0);
        DataBaseUtil.update("update chat set is_send = 1 where send_user = '"+friendID+"'", SQLLiteConstant.CHAT_TABLE);
    }
}
