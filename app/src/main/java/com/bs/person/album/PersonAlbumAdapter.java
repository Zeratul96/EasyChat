package com.bs.person.album;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bs.database.DataBaseUtil;
import com.bs.easy_chat.R;
import com.bs.function.adapter.GridViewAdapter;
import com.bs.function.comment.CommentActivity;
import com.bs.parameter.Constant;
import com.bs.parameter.Preference;
import com.bs.tool_package.ImageTools;
import com.bs.tool_package.TimeTools;
import com.bs.util.ImageTransmissionUtil;
import com.bs.util.MainHandler;
import com.bs.util.NetConnectionUtil;

import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 13273 on 2017/10/27.
 *
 */

class PersonAlbumAdapter extends RecyclerView.Adapter<PersonAlbumAdapter.ViewHolder>{

    private Context context;
    private List<Map<String,String>> detailData;
    private List<Map<String,String>> userInfoList;
    private List<Map<String,String>> favoriteList = new ArrayList<>();
    private AlertDialog alertDialog;


    PersonAlbumAdapter(Context context, List<Map<String,String>> detailData) {
        this.context = context;
        this.detailData = detailData;
        userInfoList = DataBaseUtil.queryFriends();
    }

    public PersonAlbumAdapter setData(List<Map<String,String>> detailData){
        this.detailData = detailData;
        userInfoList = DataBaseUtil.queryFriends();
        return this;
    }

    void setFavoriteList(List<Map<String,String>> list){
        favoriteList = list;
    }

    @Override
    public int getItemCount() {return detailData == null?0:detailData.size();}

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final HashMap<String,String> hashMap = (HashMap<String, String>) detailData.get(position);
        if(hashMap.get("content").length()>0){
            holder.content.setVisibility(View.VISIBLE);
            holder.content.setText(hashMap.get("content"));
        }
        else
            holder.content.setVisibility(View.GONE);

        holder.favoriteNum.setText(hashMap.get("favorite_number")+"人喜欢");
        holder.date.setText(TimeTools.parseDetailTime(hashMap.get("detail_time")));


        String name = hashMap.get("user_id");
        //是自己
        if(name.equals(Preference.userInfoMap.get("user_id"))) {
            name = Preference.userInfoMap.get("nickname");
            holder.sculpture.setImageBitmap(ImageTools.createCircleImage(ImageTransmissionUtil.loadSculptureOnlyInLocal(context,Preference.userInfoMap.get("sculpture"), true)));
            holder.deletion.setVisibility(View.VISIBLE);
        }
        //是好友
        else{
            holder.deletion.setVisibility(View.GONE);
            Map<String, String> userInfo = findUserInfo(hashMap.get("user_id"));
            if(userInfo != null){
                name = userInfo.get("remark_name").length() > 0 ? userInfo.get("remark_name") : userInfo.get("nickname");
                holder.sculpture.setImageBitmap(ImageTools.createCircleImage(ImageTransmissionUtil.loadSculptureOnlyInLocal(context, userInfo.get("sculpture"),true)));
            }
            else holder.sculpture.setImageBitmap(ImageTools.createCircleImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.no_picture)));
        }
        holder.name.setText(name);
        holder.deletion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String,String> map = new HashMap<>();
                map.put("msgType", Constant.DELETE_MOMENTS);
                map.put("moments_id", hashMap.get("moments_id"));
                showAlertDialog(map, hashMap);
            }
        });


        final List<Bitmap> bitmapList = new ArrayList<>();
        if(hashMap.get("pictures_id").length()>0) {
            String[] piece = hashMap.get("pictures_id").split("<#>");
            for (String s : piece)
                bitmapList.add(ImageTransmissionUtil.loadMomentsSculptureOnlyLocal(context, s));
        }
        holder.gridView.setAdapter(new GridViewAdapter(context, bitmapList));

        final boolean isFavorite = checkSelfFavorite(hashMap.get("moments_id"));
            holder.likeBtn.setImageDrawable(ContextCompat.getDrawable(context, isFavorite?R.drawable.red_favorite:R.drawable.gray_favorite));

        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Map<String,String> map = new HashMap<>();
                        //当前状态是喜欢 再点击则取消喜欢
                        if(isFavorite){
                            map.put("msgType", Constant.DELETE_SELF_FAVORITE);
                            map.put("moments_id", hashMap.get("moments_id"));
                            map.put("favorite_id", hashMap.get("moments_id")+"<#>"+Preference.userInfoMap.get("user_id"));
                            HashMap<String,String> changes = new HashMap<>();
                            changes.put("moments_id",hashMap.get("moments_id"));
                            hashMap.put("favorite_number", Integer.parseInt(hashMap.get("favorite_number"))-1+"");
                            favoriteList.remove(changes);
                        }
                        //当前状态不是喜欢 再点击则变为喜欢
                        else{
                            map.put("msgType", Constant.INSERT_SELF_FAVORITE);
                            map.put("moments_id", hashMap.get("moments_id"));
                            map.put("user_id", Preference.userInfoMap.get("user_id"));
                            HashMap<String,String> changes = new HashMap<>();
                            changes.put("moments_id", hashMap.get("moments_id"));
                            hashMap.put("favorite_number", Integer.parseInt(hashMap.get("favorite_number"))+1+"");
                            favoriteList.add(changes);
                        }
                        NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(), 0);
                        MainHandler.getInstance().post(new Runnable() {
                            @Override
                            public void run() {
                                PersonAlbumAdapter.this.notifyItemChanged(holder.getAdapterPosition());
                            }
                        });
                    }
                }).start();
            }
        });

        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("msg",hashMap.get("moments_id"));
                bundle.putString("author", hashMap.get("user_id"));
                context.startActivity(new Intent(context, CommentActivity.class).putExtras(bundle));
            }
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.moments_item, parent, false));
    }


    private boolean checkSelfFavorite(String momentsID){
        for(Map<String,String> map:favoriteList)
        {
            if(map.get("moments_id").equals(momentsID)) return true;
        }

        return false;
    }

    private Map<String,String> findUserInfo(@NonNull String userID){

        for (Map<String,String> map : userInfoList){
            if(map.get("friend_id").equals(userID)){
                return map;
            }
        }
        return null;
    }

    private void showAlertDialog(final Map<String,String> map, final Map<String,String> rowData) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogCustom);
        builder.setTitle("提示");
        //对话框内容
        builder.setMessage("确定要删除此条动态吗？");
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
                                if (result.equals(Constant.SERVER_CONNECTION_ERROR))
                                    Toast.makeText(context, context.getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                                else
                                    detailData.remove(rowData);
                                PersonAlbumAdapter.this.notifyDataSetChanged();
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

        TextView content;
        GridView gridView;
        ImageView sculpture;
        TextView name;
        TextView date;
        TextView favoriteNum;
        ImageView likeBtn;
        ImageView commentBtn;
        TextView deletion;

        ViewHolder(View view){
            super(view);
            name = view.findViewById(R.id.name);
            date = view.findViewById(R.id.date);
            sculpture = view.findViewById(R.id.sculpture);
            content = view.findViewById(R.id.content);
            gridView = view.findViewById(R.id.grid_view);
            favoriteNum = view.findViewById(R.id.favorite_num);
            likeBtn = view.findViewById(R.id.like);
            deletion = view.findViewById(R.id.deletion);
            commentBtn = view.findViewById(R.id.comment);
        }
    }

}
