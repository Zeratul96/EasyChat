package com.bs.main.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bs.easy_chat.R;
import com.bs.main.friends.AddFriendsActivity;
import com.bs.main.friends.FriendDetailActivity;
import com.bs.util.ImageTransmissionUtil;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 13273 on 2017/10/11.
 *
 */

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder>{

    private List<Map<String,String>> originData;
    private Context context;

    public ContactAdapter(Context context, List<Map<String,String>> mData){
        this.context = context;
        this.originData = mData;
    }

    public ContactAdapter setData(List<Map<String,String>> newData){
        originData = newData;
        return this;
    }

    @Override
    public int getItemCount() {return originData == null?0:originData.size();}

    @Override
    public ContactAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.contact_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final HashMap<String,String> map = (HashMap<String,String>) originData.get(position);
        holder.text.setText(map.get("name"));
        holder.content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(originData.get(position).get("friend_id").length()>0){
                    Bundle bundle = new Bundle();
                    bundle.putString("friend_id",originData.get(position).get("friend_id"));
                    bundle.putBoolean("send_msg_finish", false);
                    context.startActivity(new Intent(context, FriendDetailActivity.class).putExtras(bundle));
                }
                else if(originData.get(position).get("name").equals("新的朋友"))
                    context.startActivity(new Intent(context, AddFriendsActivity.class));
            }
        });

        switch (map.get("friend_id"))
        {
            case "":
                if(map.get("name").equals("新的朋友"))
                    holder.image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.new_friend));
                else
                    holder.image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.chat_group));
                break;

            default:
                holder.image.setImageBitmap(ImageTransmissionUtil.loadSculptureOnlyInLocal(context, map.get("sculpture"), true));
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        ImageView image;
        TextView text;
        View content;

        ViewHolder(View itemView){
            super(itemView);
            content = itemView.findViewById(R.id.content);
            image = itemView.findViewById(R.id.sculpture);
            text = itemView.findViewById(R.id.contact_name);
        }

    }
}
