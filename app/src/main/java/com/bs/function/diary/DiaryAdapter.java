package com.bs.function.diary;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bs.database.DataBaseUtil;
import com.bs.easy_chat.R;
import com.bs.parameter.Constant;
import com.bs.parameter.Preference;
import com.bs.parameter.SQLLiteConstant;
import com.bs.tool_package.FileHelper;
import com.bs.tool_package.ImageTools;
import com.bs.tool_package.TimeTools;
import com.bs.util.MainHandler;
import com.bs.util.MyListViewAdapter;
import com.bs.util.NetConnectionUtil;
import com.bs.widget.CustomProgress;
import com.bs.widget.MyGridView;
import com.bumptech.glide.Glide;

import net.sf.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 13273 on 2017/10/9.
 *
 */

class DiaryAdapter extends MyListViewAdapter{

    private DiaryActivity activity;
    private boolean uploadFailure;
    private Dialog alertDialog;

    DiaryAdapter(int size, DiaryActivity activity){
        super(size);
        this.activity = activity;
    }

    @Override
    public int getCount() {return activity.dataList.size();}

    @Override
    public View getView(final int i, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            convertView = LayoutInflater.from(activity).inflate(R.layout.diary_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final Map<String ,String> map = activity.dataList.get(i);
        viewHolder.title.setText(map.get("title"));
        String dateStr = map.get("date");
        viewHolder.date.setText((dateStr.equals(TimeTools.generateContentFormatTime()))?"今天  "+map.get("detail_date").substring(13,18):dateStr);

        if(map.get("content").length()>0){
            viewHolder.content.setVisibility(View.VISIBLE);
            viewHolder.content.setText(map.get("content"));
        }
        else
            viewHolder.content.setVisibility(View.GONE);


        final String[] picturePaths = map.get("pictures").split("<#>");
        if(picturePaths.length > 0 && !picturePaths[0].equals("")){
            viewHolder.gridView.setVisibility(View.VISIBLE);
            viewHolder.gridView.setAdapter(new GridViewAdapter(picturePaths));
        }

        else
            viewHolder.gridView.setVisibility(View.GONE);


        viewHolder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog(i);
            }
        });

        viewHolder.deployBtn.setEnabled(true);
        viewHolder.deployBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Dialog progress = CustomProgress.show(activity, "上传中…",false, null);
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        List<String> momentsPicturePath = new ArrayList<>();
                        uploadFailure = false;
                        String picturePathStr = "";

                        if(picturePaths.length > 0 && !picturePaths[0].equals(""))
                        {
                            for(String string:picturePaths){
                                String picPer = "moments/"+ Preference.userInfoMap.get("user_id") +string.split("/diary_")[1];
                                picturePathStr += picPer;
                                momentsPicturePath.add(picPer);
                                Map<String,String> map = new HashMap<>();
                                map.put("msgType", Constant.UPLOAD_PICTURE);
                                map.put("picPath",picPer);
                                String pictureUploadResult = NetConnectionUtil.uploadPicture(JSONObject.fromObject(map).toString(), ImageTools.BitmapToBytes(ImageTools.getLocalBitmap(string)));
                                if(pictureUploadResult == null|| pictureUploadResult.length()==0 || pictureUploadResult .equals(Constant.SERVER_CONNECTION_ERROR)){
                                    uploadFailure = true;
                                    break;
                                }
                                picturePathStr += "<#>";
                            }
                        }

                        //继续上传文字信息
                        if(!uploadFailure){
                            Map<String,String> mapToUploadText = new HashMap<>();
                            mapToUploadText.put("msgType", Constant.INSERT_MOMENTS);
                            mapToUploadText.put("user_id", Preference.userInfoMap.get("user_id"));
                            mapToUploadText.put("content", map.get("content"));
                            mapToUploadText.put("pictures_id",picturePathStr);
                            String result=  NetConnectionUtil.uploadData(JSONObject.fromObject(mapToUploadText).toString(), 0);
                            uploadFailure = (result.equals(Constant.SERVER_CONNECTION_ERROR) || result.equals("[null]"));
                        }

                        //上传成功并且有图片则先复制这次上传的图片到本地
                        int count = 0;
                        if(!uploadFailure && !momentsPicturePath.isEmpty()){
                            for (String s:picturePaths){
                                String localPath = FileHelper.getDiskCacheDir(activity)+"/moments_"+ momentsPicturePath.get(count++).substring(8);
                                FileHelper.copyFile(new File(s),localPath);
                            }
                        }

                        MainHandler.getInstance().post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activity, uploadFailure?"上传失败":"上传成功" , Toast.LENGTH_SHORT).show();
                                progress.dismiss();
                            }
                        });
                    }
                }).start();
            }
        });


        return convertView;
    }

    private void deleteDiary(int i){
        String sql = "delete from diary where primary_key = '"+activity.dataList.get(i).get("primary_key")+"'";
        DataBaseUtil.delete(sql, SQLLiteConstant.NOTES_TABLE);
    }


    private class ViewHolder{

        TextView date;
        TextView title;
        TextView content;
        GridView gridView;
        Button deployBtn;
        Button deleteBtn;

        ViewHolder(View view){
            date  = view.findViewById(R.id.date);
            title = view.findViewById(R.id.title);
            content = view.findViewById(R.id.content);
            gridView = view.findViewById(R.id.grid_view);
            deployBtn = view.findViewById(R.id.deploy_btn);
            deleteBtn = view.findViewById(R.id.del_btn);
        }
    }

    private class GridViewAdapter extends MyListViewAdapter{

        String[] path;

        GridViewAdapter(String[] path){
            super(path.length);
            this.path = path;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            CreateDiaryActivity.ViewHolder holder;

            if(view == null){
                view = LayoutInflater.from(activity).inflate(R.layout.picture_item, viewGroup, false);
                holder = new CreateDiaryActivity.ViewHolder((ImageView) view.findViewById(R.id.picture));
                view.setTag(holder);
            }
            else{
                holder = (CreateDiaryActivity.ViewHolder) view.getTag();
            }

            if(((MyGridView)viewGroup).isOnMeasure)
                return view;

            Glide.with(activity).
                    load(new File(path[i])).
                    centerCrop().
                    into(holder.imageView);
            return view;
        }
    }

    private void showAlertDialog(final int i) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AlertDialogCustom);
        builder.setTitle("提示");
        //对话框内容
        builder.setMessage("确定要删除此条日记吗？");
        //确定按钮
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteDiary(i);
                activity.dataList.remove(i);
                DiaryAdapter.this.notifyDataSetChanged();
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
}

