package com.bs.function.comment;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
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
import com.bs.util.MyListViewAdapter;
import com.bs.util.NetConnectionUtil;
import com.bs.widget.MyListView2;

import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 13273 on 2017/11/4.
 *
 */

class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder>{

    private String momentsAuthor;
    private Context context;
    private List<Map<String,String>> data;
    private List<Map<String,String>> parentData = new ArrayList<>();//根数据
    private List<Map<String,String>> userInfoList;
    private OnTouchContentListener listener;
    private AlertDialog alertDialog;

     CommentAdapter(Context context, List<Map<String,String>> data, String momentsAuthor){
        this.momentsAuthor = momentsAuthor;
        this.context = context;
        this.data = data;
        userInfoList = DataBaseUtil.queryFriends();
    }

    public CommentAdapter setData(List<Map<String, String>> data) {
        this.data = data;
        return this;
    }

    private void findMainComments(){
        parentData.clear();
        for(Map<String,String> map: data){
            if(map.get("comment_id").equals(map.get("parent_id")))
                parentData.add(map);
        }
    }

    @Override
    public int getItemCount() {
        if(data == null) return 0;
        findMainComments();
        return parentData.size();
    }

    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.comments_item, parent, false));
    }

    @Override
    public void onBindViewHolder(CommentAdapter.ViewHolder holder, int position) {
        final HashMap<String,String> hashMap = (HashMap<String, String>) parentData.get(position);

        String name = hashMap.get("user_id");
        //是自己的评论
        if(name.equals(Preference.userInfoMap.get("user_id"))) {
            name = Preference.userInfoMap.get("nickname");
            holder.sculpture.setImageBitmap(ImageTools.createCircleImage(ImageTransmissionUtil.loadSculptureOnlyInLocal(context,Preference.userInfoMap.get("sculpture"), true)));
            holder.deletion.setVisibility(View.VISIBLE);
        }
        //是好友的评论
        else{
            //判断是不是自己的说说 如果是也可以删除评论
            holder.deletion.setVisibility(momentsAuthor.equals(Preference.userInfoMap.get("user_id"))?View.VISIBLE:View.GONE);
            Map<String, String> userInfo = findUserInfo(hashMap.get("user_id"));
            if(userInfo != null){
                name = userInfo.get("remark_name").length() > 0 ? userInfo.get("remark_name") : userInfo.get("nickname");
                holder.sculpture.setImageBitmap(ImageTools.createCircleImage(ImageTransmissionUtil.loadSculptureOnlyInLocal(context, userInfo.get("sculpture"),true)));
            }
            else holder.sculpture.setImageBitmap(ImageTools.createCircleImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.no_picture)));
        }
        holder.deletion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String,String> map = new HashMap<>();
                map.put("msgType", Constant.DELETE_COMMENT);
                map.put("comment_id", hashMap.get("comment_id"));
                showAlertDialog(map, hashMap);
            }
        });

        holder.name.setText(name);
        holder.mainContent.setText(hashMap.get("content"));
        if(!hashMap.get("user_id").equals(Preference.userInfoMap.get("user_id")))
            holder.mainContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener!=null)
                        listener.onTouchContent(hashMap.get("comment_id"), hashMap.get("user_id"));
                }
            });
        holder.date.setText(TimeTools.parseDetailTime(hashMap.get("detail_time")));

        //这一楼层的其他回复
        final List<Map<String,String>> callBack = new ArrayList<>();
        findCallBack(hashMap.get("comment_id"), callBack);
        if(!callBack.isEmpty()){
            MyListViewAdapter adapter = new MyListViewAdapter(callBack.size()) {
                @Override
                public View getView(int i, View view, ViewGroup viewGroup) {
                    final Map<String,String> mapInfo = callBack.get(i);
                    CallBackViewHolder viewHolder;
                    if(view == null){
                        view = LayoutInflater.from(context).inflate(R.layout.call_back_item, viewGroup, false);
                        viewHolder = new CallBackViewHolder(view);
                        view.setTag(viewHolder);
                    }
                    else{
                        viewHolder = (CallBackViewHolder) view.getTag();
                    }

                    viewHolder.callBackContent.setText(mapInfo.get("content"));
                    String userName = defineUserName(mapInfo.get("user_id"));
                    String objectName = defineUserName(mapInfo.get("object_user"));

                    viewHolder.callBackInfo.setText(userName+"  回复  "+objectName+"：");
                    if(!Preference.userInfoMap.get("user_id").equals(mapInfo.get("user_id")))
                        viewHolder.callBackContent.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(listener != null)
                                    listener.onTouchContent(mapInfo.get("comment_id"), mapInfo.get("user_id"));
                            }
                        });

                    return view;
                }
            };
            holder.callBack.setAdapter(adapter);
        }
        else holder.callBack.setAdapter(null);
    }

    private Map<String,String> findUserInfo(@NonNull String userID){
        for (Map<String,String> map : userInfoList)
            if(map.get("friend_id").equals(userID))
                return map;
        return null;
    }

    private String defineUserName(String ID){
        String name = ID;
        if(name.equals(Preference.userInfoMap.get("user_id")))
            name = Preference.userInfoMap.get("nickname");
        else{
            Map<String, String> userInfo = findUserInfo(name);
            if(userInfo != null){
                name = userInfo.get("remark_name").length() > 0 ? userInfo.get("remark_name") : userInfo.get("nickname");
            }
        }
        return name;
    }

    private void findCallBack(String parentID,List<Map<String,String>> resultList){
        for(Map<String,String> map:data){
            if(map.get("parent_id").equals(parentID) && !map.get("comment_id").equals(parentID)){
                resultList.add(map);
                findCallBack(map.get("comment_id"), resultList);
            }
        }
    }

    private void showAlertDialog(final Map<String,String> map, final Map<String,String> rowData) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogCustom);
        builder.setTitle("提示");
        //对话框内容
        builder.setMessage("确定要删除此条评论吗？");
        //确定按钮
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final String result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(), 0);
                        MainHandler.getInstance().post(new Runnable() {
                            @Override
                            public void run() {
                                if(!result.equals(Constant.SERVER_CONNECTION_ERROR) && !result.equals("[null]")){
                                    data.remove(rowData);
                                    CommentAdapter.this.notifyDataSetChanged();
                                    DataBaseUtil.delete("delete from comment where comment_id = '"+map.get("comment_id")+"'", SQLLiteConstant.COMMENT_TABLE);
                                }
                                else
                                    Toast.makeText(context, context.getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).start();
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


    private class CallBackViewHolder{
        TextView callBackInfo;
        TextView callBackContent;

        CallBackViewHolder(View view){
            callBackContent = view.findViewById(R.id.call_back_content);
            callBackInfo = view.findViewById(R.id.call_back_info);
        }
    }

    /**
     * 触摸回复内容回调接口
     */
   interface OnTouchContentListener{
        void onTouchContent(String commentID, String objectUser);
    }

    void setOnTouchContentListener(OnTouchContentListener l){
        listener = l;
    }
}
