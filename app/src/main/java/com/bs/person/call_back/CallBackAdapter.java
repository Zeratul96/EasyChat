package com.bs.person.call_back;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bs.database.DataBaseUtil;
import com.bs.easy_chat.R;
import com.bs.parameter.Constant;
import com.bs.parameter.Preference;
import com.bs.parameter.SQLLiteConstant;
import com.bs.tool_package.ImageTools;
import com.bs.tool_package.TimeTools;
import com.bs.util.ImageTransmissionUtil;
import com.bs.util.MainHandler;
import com.bs.util.NetConnectionUtil;
import com.bs.widget.MyListView2;

import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 13273 on 2017/11/4.
 *
 */

class CallBackAdapter extends RecyclerView.Adapter<CallBackAdapter.ViewHolder>{

    private Context context;
    private List<Map<String,String>> data;
    private List<Map<String,String>> userInfoList;
    private OnTouchContentListener listener;

     CallBackAdapter(Context context, List<Map<String,String>> data){
        this.context = context;
        this.data = data;
        userInfoList = DataBaseUtil.queryFriends();
    }

    public CallBackAdapter setData(List<Map<String,String>> data){
        this.data = data;
        userInfoList = DataBaseUtil.queryFriends();
        return this;
    }

    @Override
    public int getItemCount() {return data==null?0:data.size();}

    @Override
    public CallBackAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.comments_item, parent, false));
    }

    @Override
    public void onBindViewHolder(CallBackAdapter.ViewHolder holder, int position) {
        final HashMap<String,String> hashMap = (HashMap<String, String>) data.get(position);

        String name = hashMap.get("user_id");
        //是自己的回复
        if(name.equals(Preference.userInfoMap.get("user_id"))) {
            name = Preference.userInfoMap.get("nickname");
            holder.sculpture.setImageBitmap(ImageTools.createCircleImage(ImageTransmissionUtil.loadSculptureOnlyInLocal(context,Preference.userInfoMap.get("sculpture"), true)));
        }
        //是好友的回复
        else{
            Map<String, String> userInfo = findUserInfo(hashMap.get("user_id"));
            if(userInfo != null){
                name = userInfo.get("remark_name").length() > 0 ? userInfo.get("remark_name") : userInfo.get("nickname");
                holder.sculpture.setImageBitmap(ImageTools.createCircleImage(ImageTransmissionUtil.loadSculptureOnlyInLocal(context, userInfo.get("sculpture"),true)));
            }
            else holder.sculpture.setImageBitmap(ImageTools.createCircleImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.no_picture)));
        }
        holder.name.setText(name);

        holder.deletion.setTextColor(Color.parseColor("#8c8c8c"));
        holder.deletion.setText("清除");
        holder.deletion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Map<String,String> map = new HashMap<>();
                map.put("msgType", Constant.CLEAR_CALLBACK);
                map.put("comment_id", hashMap.get("comment_id"));
                map.put("mode","0");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final String result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(), 0);
                        MainHandler.getInstance().post(new Runnable() {
                            @Override
                            public void run() {
                                if(!result.equals(Constant.SERVER_CONNECTION_ERROR) && !result.equals("[null]")){
                                    data.remove(hashMap);
                                    DataBaseUtil.delete("delete from callback where comment_id = '"+map.get("comment_id")+"'", SQLLiteConstant.CALL_BACK);
                                    CallBackAdapter.this.notifyDataSetChanged();
                                }
                                else
                                    Toast.makeText(context, context.getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).start();
            }
        });
        holder.mainContent.setText(hashMap.get("content"));
        holder.mainContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener!=null)
                    listener.onTouchContent(hashMap.get("moments_id"), hashMap.get("author_id"));
            }
        });
        holder.date.setText(TimeTools.parseDetailTime(hashMap.get("detail_time")));

        holder.callBack.setVisibility(View.GONE);
    }

    private Map<String,String> findUserInfo(@NonNull String userID){
        for (Map<String,String> map : userInfoList)
            if(map.get("friend_id").equals(userID))
                return map;
        return null;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        TextView date;
        TextView deletion;
        TextView mainContent;
        ImageView sculpture;
        MyListView2 callBack;

        ViewHolder(View view){
            super(view);
            name = view.findViewById(R.id.name);
            date = view.findViewById(R.id.date);
            deletion = view.findViewById(R.id.deletion);
            mainContent = view.findViewById(R.id.main_content);
            sculpture = view.findViewById(R.id.sculpture);
            callBack = view.findViewById(R.id.call_back);
        }
    }

    /**
     * 触摸回复内容回调接口
     */
   interface OnTouchContentListener{
        void onTouchContent(String momentsID, String authorID);
    }

    void setOnTouchContentListener(OnTouchContentListener l){
        listener = l;
    }
}
