package com.bs.main.chat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bs.easy_chat.R;
import com.bs.main.friends.FriendDetailActivity;
import com.bs.parameter.Preference;
import com.bs.tool_package.TimeTools;
import com.bs.util.ImageTransmissionUtil;
import com.bs.util.MainHandler;

import java.util.List;
import java.util.Map;

/**
 * Created by 13273 on 2017/11/16.
 *聊天记录数据流格式
 * type
 * content
 * send_user
 * receive_user
 * time
 * state 0:正在发送  1:发送成功  -1:发送失败
 */

class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder>{

    private String picturePath;
    private List<Map<String,String>> dataList;
    private Context context;

    private int[] emoji = new int[]
    {
        R.drawable.high_emoji_1,R.drawable.high_emoji_2,R.drawable.high_emoji_3,R.drawable.high_emoji_4,R.drawable.high_emoji_5,R.drawable.high_emoji_6,
        R.drawable.high_emoji_7,R.drawable.high_emoji_8,R.drawable.high_emoji_9,R.drawable.high_emoji_10,R.drawable.high_emoji_11,R.drawable.high_emoji_12,
        R.drawable.high_emoji_13,R.drawable.high_emoji_14,R.drawable.high_emoji_15,R.drawable.high_emoji_16,R.drawable.high_emoji_17,R.drawable.high_emoji_18,
        R.drawable.high_emoji_19,R.drawable.high_emoji_20,R.drawable.high_emoji_21,R.drawable.high_emoji_22,R.drawable.high_emoji_23,R.drawable.high_emoji_24,
        R.drawable.high_emoji_25,R.drawable.high_emoji_26,R.drawable.high_emoji_27,R.drawable.high_emoji_28,R.drawable.high_emoji_29,R.drawable.high_emoji_30,
    };

    ChatAdapter(List<Map<String,String>> dataList, Context context, String picturePath){
        this.context = context;
        this.dataList = dataList;
        this.picturePath = picturePath;
    }

    public ChatAdapter setData(List<Map<String,String>> dataList){
        this.dataList = dataList;
        return this;
    }

    @Override
    public int getItemCount() { return dataList == null ? 0:dataList.size();}

    @Override
    public int getItemViewType(int position) {
        //右边（自己）是0 左边是1
        return dataList.get(position).get("send_user").equals(Preference.userInfoMap.get("user_id"))?0:1;
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ChatViewHolder(LayoutInflater.from(context).inflate(viewType==0?R.layout.right_message_item:R.layout.left_message_item, parent, false), viewType);
    }

    @Override
    public void onBindViewHolder(final ChatViewHolder holder, int position) {
        final Map<String,String> msg = dataList.get(position);
        switch (msg.get("type")){
            case "text":
                holder.emoji.setVisibility(View.GONE);
                holder.imageContent.setVisibility(View.GONE);
                holder.textContent.setVisibility(View.VISIBLE);
                holder.textContent.setText(msg.get("content"));
                break;

            case "picture":
                holder.emoji.setVisibility(View.GONE);
                holder.textContent.setVisibility(View.GONE);
                holder.imageContent.setVisibility(View.VISIBLE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final Bitmap bitmap = ImageTransmissionUtil.loadChatImage(context, msg.get("content"));
                            MainHandler.getInstance().post(new Runnable() {
                                @Override
                                public void run() {
                                    holder.imageContent.setImageBitmap(bitmap == null?BitmapFactory.decodeResource(context.getResources(), R.drawable.sculpture_loading_failure): bitmap);
                                }
                            });
                        }
                }).start();
                break;

            case "emoji":
                holder.textContent.setVisibility(View.GONE);
                holder.imageContent.setVisibility(View.GONE);
                holder.emoji.setVisibility(View.VISIBLE);
                holder.emoji.setImageDrawable(ContextCompat.getDrawable(context, emoji[Integer.parseInt(msg.get("content"))]));
                break;
        }
        if(holder.viewType == 1){
            holder.sculpture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString("friend_id", msg.get("send_user"));
                    bundle.putString("back_type", "返回");
                    bundle.putBoolean("send_msg_finish", true);
                    context.startActivity(new Intent(context, FriendDetailActivity.class).putExtras(bundle));
                }
            });
        }

        Bitmap bitmap  = ImageTransmissionUtil.loadSculptureOnlyInLocal(context, holder.viewType==0?Preference.userInfoMap.get("sculpture"):picturePath,false);
        if (bitmap == null) bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.no_sculpture);
        holder.sculpture.setImageBitmap(bitmap);

        if(shouldShowTime(position)) {
            holder.time.setVisibility(View.VISIBLE);
            holder.time.setText(TimeTools.parseChatTime(msg.get("time")));
        }
        else
            holder.time.setVisibility(View.GONE);


        //正在发送
        if(holder.viewType == 0){
            switch (msg.get("state")){
                case "0":
                    holder.sendingFlag.setVisibility(View.VISIBLE);
                    holder.sendingFlag.setBackground(ContextCompat.getDrawable(context, R.drawable.progressbar));
                    ((AnimationDrawable) holder.sendingFlag.getBackground()).start();
                    break;

                case "1":
                    holder.sendingFlag.setVisibility(View.GONE);
                    break;

                case "-1":
                    holder.sendingFlag.setVisibility(View.VISIBLE);
                    holder.sendingFlag.setBackground(ContextCompat.getDrawable(context, R.drawable.sending_failure));
                    break;
            }
        }
    }

    /**
     * 每隔四分钟显示一次时间
     * @return 是否需要显示时间
     */
    private boolean shouldShowTime(int position){
        if(position - 1 < 0) return true;
        String[] lastTime = dataList.get(position - 1).get("time").split("-");
        String[] presentTime = dataList.get(position).get("time").split("-");
        if(!lastTime[0].equals(presentTime[0])||!lastTime[1].equals(presentTime[1])||!lastTime[2].equals(presentTime[2])) return true;

        int last = Integer.parseInt(lastTime[3])*60+Integer.parseInt(lastTime[4]);
        int present = Integer.parseInt(presentTime[3])*60+Integer.parseInt(presentTime[4]);

        return !(present - last < 4);
    }

    class ChatViewHolder extends RecyclerView.ViewHolder{

        int viewType;
        TextView time;
        TextView textContent;
        ImageView sculpture;
        ImageView emoji;
        ImageView imageContent;
        ImageView sendingFlag;

        ChatViewHolder(View itemView, int viewType){
            super(itemView);
            this.viewType = viewType;
            time = itemView.findViewById(R.id.time);
            textContent = itemView.findViewById(R.id.text_content);
            sculpture = itemView.findViewById(R.id.sculpture);
            emoji = itemView.findViewById(R.id.emoji);
            imageContent = itemView.findViewById(R.id.image_content);
            if(viewType==0)
                sendingFlag = itemView.findViewById(R.id.sending_flag);
        }
    }
}
