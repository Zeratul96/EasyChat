package com.bs.main.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bs.database.DataBaseUtil;
import com.bs.easy_chat.R;
import com.bs.parameter.SQLLiteConstant;
import com.bs.tool_package.ExpressParse;
import com.bs.util.ImageTransmissionUtil;
import com.bs.util.MyListViewAdapter;
import com.bs.widget.BadgeView;
import com.bs.widget.SwipeListView;

import java.util.List;
import java.util.Map;

/**
 * Created by 13273 on 2017/11/20.
 *
 */

public class MessageAdapter extends MyListViewAdapter{

    private AlertDialog alertDialog;
    private Context context;
    private List<Map<String,String>> messageList;//消息记录条目 格式：contact_with\last_time\last_type\last_content_brief
    private LinearLayout.LayoutParams lp1;
    private LinearLayout.LayoutParams lp2;
    private List<Map<String,String>> friendInfoList;
    private Map<String,Integer> contactWith;
    private SwipeListView swipeListView;

    public MessageAdapter(List<Map<String,String>> messageTitleList, Map<String, Integer> contactWith, Context context, int mRightWidth){
        super(messageTitleList==null?0:messageTitleList.size());
        this.messageList = messageTitleList;
        this.contactWith = contactWith;
        this.context = context;
        lp1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        lp2 = new LinearLayout.LayoutParams(mRightWidth, LinearLayout.LayoutParams.MATCH_PARENT);
        friendInfoList = DataBaseUtil.queryFriends();
    }

    public MessageAdapter updateFriendInfo(){
        friendInfoList = DataBaseUtil.queryFriends();
        return this;
    }

    public void setSwipeListView(SwipeListView swipeListView){
        this.swipeListView = swipeListView;
    }

    @Override
    public int getCount() {return messageList==null?0:messageList.size();}

    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        MessageViewHolder holder;
        final Map<String,String> infoMap = messageList.get(position);

        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.message_item, viewGroup, false);
            holder = new MessageViewHolder(convertView);
            convertView.setTag(holder);
        }
        else holder = (MessageViewHolder) convertView.getTag();

        holder.itemLeft.setLayoutParams(lp1);
        holder.itemRight.setLayoutParams(lp2);
        Map<String,String> friendMap = findTargetFriend(infoMap.get("contact_with"));

        holder.time.setText(infoMap.get("last_time"));
        holder.sculpture.setImageBitmap(ImageTransmissionUtil.loadSculptureOnlyInLocal(context, friendMap.get("sculpture"), true));

        if(contactWith.get(infoMap.get("contact_with")) != 0){
            BadgeView badgeView = new BadgeView(context);
            badgeView.setBadgeCount(contactWith.get(infoMap.get("contact_with")));
            badgeView.setTargetView(holder.sculpture);
        }

        holder.name.setText(friendMap.get("remark_name").length()==0?friendMap.get("nickname"):friendMap.get("remark_name"));
        switch (infoMap.get("last_type")){
            case "text":holder.contentBrief.setText(infoMap.get("last_content_brief"));
                break;

            case "emoji":holder.contentBrief.setText(ExpressParse.parseExpress((infoMap.get("last_content_brief"))));
                break;

            case "picture":
                holder.contentBrief.setText("[图片]");
                break;
        }

        holder.itemRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null){
                    mListener.onRightItemClick(v, position);
                }
            }
        });

        return convertView;
    }

    private Map<String,String> findTargetFriend(String userID){
        for(Map<String,String> perData:friendInfoList){
            if(perData.get("friend_id").equals(userID))
                return perData;
        }
        return null;
    }

    public void showAlertDialog(final Map<String,String> titleMap) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogCustom);
        builder.setTitle("提示");
        //对话框内容
        builder.setMessage("删除后，将清空该聊天的消息记录");
        //确定按钮
        builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DataBaseUtil.delete("delete from chat where send_user = '"+titleMap.get("contact_with")+"' or receive_user = '"+titleMap.get("contact_with")+"'", SQLLiteConstant.CHAT_TABLE);
                contactWith.remove(titleMap.get("contact_with"));
                messageList.remove(titleMap);
                MessageAdapter.this.notifyDataSetChanged();
                swipeListView.shrinkByDeletion();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });

        //固定对话框使其不可被取消
        builder.setCancelable(false);
        //创建对话框
        alertDialog = builder.create();
        alertDialog.show();
    }


    /**
     * 右边点击事件监听器
     */
    private onRightItemClickListener mListener = null;

    public void setOnRightItemClickListener(onRightItemClickListener listener){
        mListener = listener;
    }

    public interface onRightItemClickListener {
        void onRightItemClick(View v, int position);
    }



    private class MessageViewHolder{
        LinearLayout itemLeft;
        RelativeLayout itemRight;
        ImageView sculpture;
        TextView name;
        TextView contentBrief;
        TextView time;
        TextView itemRightDelete;

        MessageViewHolder(View itemView){
            sculpture = itemView.findViewById(R.id.sculpture);
            name = itemView.findViewById(R.id.name);
            contentBrief = itemView.findViewById(R.id.content_brief);
            time = itemView.findViewById(R.id.time);
            itemRightDelete = itemView.findViewById(R.id.item_right_txt);
            itemLeft = itemView.findViewById(R.id.item_left);
            itemRight = itemView.findViewById(R.id.item_right);
        }
    }
}
