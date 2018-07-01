package com.bs.function.diary;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bs.database.DataBaseUtil;
import com.bs.easy_chat.R;
import com.bs.parameter.Preference;
import com.bs.parameter.SQLLiteConstant;
import com.bs.tool_package.FileHelper;
import com.bs.tool_package.TimeTools;
import com.bs.util.ActivityListUtil;
import com.bs.util.BaseActivity;
import com.bs.util.MyListViewAdapter;
import com.bs.widget.MyGridView;
import com.bs.widget.SelectPicturePopupWindow;
import com.bumptech.glide.Glide;
import com.yongchun.library.view.ImageSelectorActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CreateDiaryActivity extends BaseActivity implements View.OnClickListener,SelectPicturePopupWindow.OnSelectedListener,TextWatcher{
    LinearLayout backBtn;
    TextView finishBtn;

    final int TAKE_PHOTO = 2;

    SelectPicturePopupWindow selectPicturePopupWindow;

    EditText titleEdit;
    EditText contentEdit;
    boolean hasChanged;

    GridView gridView;
    AlertDialog alertDialog;

    static ArrayList<String> picturePath;
    static GridViewAdapter adapter;

    private boolean isFirstIn = true;
    Map<String ,String> infoMap;
    private String takenPhotoPath;

    private void avoidFileExposure(){
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_diary_layout);
        avoidFileExposure();

        picturePath = new ArrayList<>();

        backBtn = (LinearLayout) findViewById(R.id.back_layout);
        backBtn.setOnClickListener(this);

        finishBtn = (TextView) findViewById(R.id.finish_btn);
        finishBtn.setOnClickListener(this);

        titleEdit = (EditText) findViewById(R.id.title_edit);
        contentEdit = (EditText) findViewById(R.id.editText);

        selectPicturePopupWindow = new SelectPicturePopupWindow(this);
        selectPicturePopupWindow.setOnSelectedListener(this);

        gridView = (GridView) findViewById(R.id.grid_view);
    }

    @Override
    protected void onStart() {
        super.onStart();

        infoMap = (HashMap<String,String>)getIntent().getSerializableExtra("msg");
        if(infoMap != null && isFirstIn){
            isFirstIn = false;
            titleEdit.setText(infoMap.get("title"));
            contentEdit.setText(infoMap.get("content"));
            String[] picPathArray = infoMap.get("pictures").split("<#>");

            if(!picPathArray[0].equals("")) picturePath = new ArrayList<>(Arrays.asList(picPathArray));
            contentEdit.requestFocus();
            contentEdit.setSelection(contentEdit.getText().toString().length());

            ((TextView)findViewById(R.id.detail_date)).setText((infoMap.get("date").equals(TimeTools.generateContentFormatTime()))? "今天  "+infoMap.get("detail_date").substring(13, 18):infoMap.get("detail_date"));
        }
        else ((TextView)findViewById(R.id.detail_date)).setText("今天  "+TimeTools.generateCurrentTime());

        adapter = new GridViewAdapter();
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int pictureNum = picturePath.size();
                if(pictureNum<9&&position==pictureNum){
                    contentEdit.clearFocus();
                    selectPicturePopupWindow.showPopupWindow(CreateDiaryActivity.this);
                }
                else{
                    Bundle bundle = new Bundle();
                    bundle.putInt("position", position);
                    startActivity(new Intent(CreateDiaryActivity.this, ImageActivity.class).putExtras(bundle));
                }
            }
        });

        titleEdit.addTextChangedListener(this);
        contentEdit.addTextChangedListener(this);

    }

    @Override
    public void onClick(View v) {
        if(v == backBtn){
            if(hasChanged)
                showAlertDialog();

            else finish();
        }
        else{
            if(infoMap != null)
            {
                if(!titleEdit.getText().toString().equals("")||!contentEdit.getText().toString().equals("")||!picturePath.isEmpty()){

                    String sql = "update diary set title = '"
                            +titleEdit.getText().toString()
                            +"',content = '"+contentEdit.getText().toString()
                            +"',pictures = '"+changeListToString()
                            +"' where primary_key = '"+infoMap.get("primary_key")
                            +"'";

                    DataBaseUtil.update(sql, SQLLiteConstant.DIARY_TABLE);
                }

                else{
                    String sql = "delete from diary where primary_key = '"+infoMap.get("primary_key")+"'";
                    DataBaseUtil.delete(sql,SQLLiteConstant.DIARY_TABLE);
                }

            }

            else if(!titleEdit.getText().toString().equals("")||!contentEdit.getText().toString().equals("")||!picturePath.isEmpty())
            {
                String sql = "insert into diary values("
                        + "'"+TimeTools.generateNumberByTime()+"'"
                        + ",'"+TimeTools.generateContentFormatTime()+"'"
                        + ",'"+TimeTools.generateDetailTime()+"'"
                        + ",'"+titleEdit.getText().toString()+"'"
                        + ",'"+contentEdit.getText().toString()+"'"
                        + ",'"+ Preference.userInfoMap.get("user_id")+"'"
                        + ",'"+changeListToString()+"')";
                DataBaseUtil.insert(sql, SQLLiteConstant.DIARY_TABLE);
            }
            finish();
        }
    }

    @Override
    public void OnSelected(View v, int position) {

        switch (position)
        {
            case 0:
                takePhotos();
                selectPicturePopupWindow.dismissPopupWindow();
                break;

            case 1:
                requestPermissionAndPickPhoto();
                selectPicturePopupWindow.dismissPopupWindow();
                break;

            case 2:
                selectPicturePopupWindow.dismissPopupWindow();
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode != RESULT_OK) return;

        switch (requestCode){
            case ImageSelectorActivity.REQUEST_IMAGE:
                ArrayList<String> result = (ArrayList<String>)data.getSerializableExtra(ImageSelectorActivity.REQUEST_OUTPUT);
                for(String s:result){
                    String[] piece = s.split("\\.");
                    String path = FileHelper.getDiskCacheDir(this)+"/diary_"+TimeTools.generateNumberByTime()+"."+piece[piece.length-1];
                    FileHelper.copyFile(new File(s), path);
                    picturePath.add(path);
                }

                if(!hasChanged) hasChanged = true;
                adapter.notifyDataSetChanged();
                break;

            case TAKE_PHOTO:
                picturePath.add(takenPhotoPath);
                if(!hasChanged) hasChanged = true;
                adapter.notifyDataSetChanged();
                break;
        }
    }

    //动态申请权限并且调用选照片
    private void requestPermissionAndPickPhoto(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED||
           ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)

           ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        else
           ImageSelectorActivity.start(this, 9 - picturePath.size(), ImageSelectorActivity.MODE_MULTIPLE, false, true, false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if(grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                    ImageSelectorActivity.start(this, 9 - picturePath.size(), ImageSelectorActivity.MODE_MULTIPLE, false, true, false);
                }
                break;

            default:break;
        }
    }

    private void takePhotos(){
        Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //下面这句指定调用相机拍照后的照片存储的路径
        takenPhotoPath = FileHelper.getDiskCacheDir(this)+ "/diary_"+TimeTools.generateNumberByTime()+".jpg";

        takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(takenPhotoPath)));
        startActivityForResult(takeIntent, TAKE_PHOTO);
    }

    private String changeListToString(){

        String str = "";
        for(String s:picturePath)
            str += s+"<#>";

        return str;
    }

    private void showAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.AlertDialogCustom);builder.setTitle("提示");
        //对话框内容
        builder.setMessage("确定要放弃此次编辑并退出吗？");
        //确定按钮
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
            }
        });

        //固定对话框使其不可被取消
        builder.setCancelable(false);

        //创建对话框
        alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(!hasChanged)
            hasChanged = true;
    }

    @Override
    public void afterTextChanged(Editable s) {}

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    class GridViewAdapter extends MyListViewAdapter{
        int itemNum;
        boolean hasAddBtn;

        GridViewAdapter(){
            super(picturePath.size());
        }

        LayoutInflater inflater = LayoutInflater.from(CreateDiaryActivity.this);

        @Override
        public int getCount() {
            itemNum = picturePath.size()<9?picturePath.size()+1:9;
            hasAddBtn = picturePath.size()<9;
            return itemNum;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder;

            if(view == null){
                view = inflater.inflate(R.layout.picture_item, viewGroup ,false);
                holder = new ViewHolder((ImageView) view.findViewById(R.id.picture));
                view.setTag(holder);
            }
            else{
                holder = ( ViewHolder) view.getTag();
            }

            if(((MyGridView)viewGroup).isOnMeasure)
                return view;

            //如果是最后一项
            if(i == itemNum-1){
                if(hasAddBtn){
                    holder.imageView.setImageDrawable(ContextCompat.getDrawable(CreateDiaryActivity.this, R.drawable.add_picture));
                }
                else{
                    Glide.with(CreateDiaryActivity.this).
                            load(new File(picturePath.get(i))).
                            centerCrop().
                            into(holder.imageView);
                }
            }

            else{
                Glide.with(CreateDiaryActivity.this).
                        load(new File(picturePath.get(i))).
                        centerCrop().
                        into(holder.imageView);
            }
            return view;
        }
    }

    static class ViewHolder{
        ImageView imageView;

        ViewHolder(ImageView iv){imageView = iv;}
    }

}
