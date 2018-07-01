package com.bs.main.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bs.database.DataBaseUtil;
import com.bs.easy_chat.R;
import com.bs.main.friends.FriendDetailActivity;
import com.bs.main.friends.ProtentialFriendActivity;
import com.bs.util.ImageTransmissionUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 13273 on 2017/10/25.
 *
 */

public class NewFriendAdapter extends RecyclerView.Adapter<NewFriendAdapter.ViewHolder>{

    private Context context;
    /**
     * nickname
     * user_id
     * gender
     * areas
     * sculpture
     * self_introduction
     * handwriting
     * already_friend
     */
    private List<Map<String,String>> detailData;
    private List<Map<String,String>> friendData;


    public NewFriendAdapter(Context context, List<Map<String,String>> detailData){
        this.context = context;
        this.detailData = detailData;
    }

    public NewFriendAdapter setData(List<Map<String,String>> detailData){
        this.detailData = detailData;
        friendData = DataBaseUtil.queryAllFriendsID();
        return this;
    }

    @Override
    public int getItemCount() {return this.detailData == null? 0: detailData.size();}

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.contact_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final HashMap<String,String> map = (HashMap<String, String>) detailData.get(position);
        holder.contactName.setText(map.get("nickname"));
        holder.sculpture.setImageBitmap(ImageTransmissionUtil.loadSculptureOnlyInLocal(context, map.get("sculpture"), true));
        final Map<String,String> compareMap = new HashMap<>();
        compareMap.put("friend_id", map.get("user_id"));

        holder.content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //该用户不是自己的好友
                if(!friendData.contains(compareMap)){
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("is_need_reload_data", false);
                    bundle.putBoolean("is_request", false);
                    context.startActivity(new Intent(context, ProtentialFriendActivity.class).putExtra("msg",map).putExtras(bundle));
                }
                //已经是自己的好友
                else{
                    Bundle bundle = new Bundle();
                    bundle.putString("friend_id", map.get("user_id"));
                    bundle.putBoolean("send_msg_finish", false);
                    context.startActivity(new Intent(context, FriendDetailActivity.class).putExtras(bundle));
                }
            }
        });

    }

    class ViewHolder extends RecyclerView.ViewHolder{

        ImageView sculpture;
        TextView contactName;
        View content;

        ViewHolder(View itemView){
            super(itemView);
            sculpture = itemView.findViewById(R.id.sculpture);
            contactName = itemView.findViewById(R.id.contact_name);
            content = itemView.findViewById(R.id.content);
        }
    }
}
