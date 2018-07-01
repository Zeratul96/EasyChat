package com.bs.person.album;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import com.bs.tool_package.FastJSON;
import com.bs.tool_package.FileHelper;
import com.bs.tool_package.ImageTools;
import com.bs.tool_package.TimeTools;
import com.bs.util.BaseActivity;
import com.bs.util.ImageTransmissionUtil;
import com.bs.util.LocalDataIOUtil;
import com.bs.util.MainHandler;
import com.bs.util.NetConnectionUtil;
import com.bs.widget.SelectPicturePopupWindow;
import com.bumptech.glide.Glide;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.yongchun.library.view.ImageSelectorActivity;

import net.sf.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *Intent:title_name
 * user_id:被看人的ID
 * moments_background
 */

public class PersonAlbumActivity extends BaseActivity implements XRecyclerView.LoadingListener,SelectPicturePopupWindow.OnSelectedListener{

    View header;
    ImageView momentsBackground;
    private SelectPicturePopupWindow selectPicturePopupWindow;

    XRecyclerView xRecyclerView;
    PersonAlbumAdapter adapter;
    List<Map<String,String>> dataList;
    List<Map<String,String>> favoriteList;
    private String backgroundPath;
    boolean isSelfAlbum;

    final int TAKE_PHOTO = 2;

    private void avoidFileExposure(){
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.moments_layout);
        avoidFileExposure();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");
        if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(getIntent().getExtras().get("user_id").equals(Preference.userInfoMap.get("user_id")))
            isSelfAlbum = true;
        ((TextView)findViewById(R.id.title_name)).setText(getIntent().getExtras().getString("title_name"));

        xRecyclerView = (XRecyclerView) findViewById(R.id.recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(layoutManager);

        xRecyclerView.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);//刷新动画风格
        xRecyclerView.setLoadingMoreProgressStyle(ProgressStyle.LineScalePulseOutRapid);//加载更多风格
        xRecyclerView.setArrowImageView(R.drawable.ic_pulltorefresh_arrow);
        xRecyclerView.setLoadingListener(this);

        header = LayoutInflater.from(this).inflate(R.layout.album_header_layout,(ViewGroup)findViewById(android.R.id.content), false);
        xRecyclerView.addHeaderView(header);
        momentsBackground = header.findViewById(R.id.moment_background);
        ((SimpleItemAnimator)xRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        adapter = new PersonAlbumAdapter(this, dataList);
        xRecyclerView.setAdapter(adapter);
        initBackground();
        queryAlbum();

        if(isSelfAlbum){
            selectPicturePopupWindow = new SelectPicturePopupWindow(this);
            selectPicturePopupWindow.resetBackGroundView();
            selectPicturePopupWindow.setOnSelectedListener(this);
            momentsBackground.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectPicturePopupWindow.showPopupWindow(PersonAlbumActivity.this);
                }
            });
        }
    }

    /*加载那一刻背景*/
    private void initBackground(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = ImageTransmissionUtil.loadMomentBackground(PersonAlbumActivity.this, getIntent().getExtras().getString("moments_background"));
                if(bitmap != null)
                    MainHandler.getInstance().post(new Runnable() {
                        @Override
                        public void run() {
                            momentsBackground.setImageBitmap(bitmap);
                        }
                    });
            }
        }).start();
    }


    private void queryAlbum() {
        //快速初始化界面
        dataList = DataBaseUtil.queryAlbumProgressively("now", getIntent().getExtras().getString("user_id"));
        adapter.setData(dataList);
        favoriteList = DataBaseUtil.queryFavorite();
        adapter.setFavoriteList(favoriteList);
        adapter.notifyDataSetChanged();
        new Thread(new Runnable() {
            @Override
            public void run() {
                loadSelfFavoriteInfo();
                Map<String, String> map = new HashMap<>();
                map.put("msgType", Constant.QUERY_ALBUM_PROGRESSIVELY);
                map.put("range", "now");
                map.put("mode", isSelfAlbum?"1":"0");
                map.put("user_id", getIntent().getExtras().getString("user_id"));
                if(!isSelfAlbum) map.put("self_id", Preference.userInfoMap.get("user_id"));

                final String result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(), 0);
                MainHandler.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        onLoadDataResult(result, false, null);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    @Override
    public void onRefresh() {refreshThread(true);}

    @Override
    public void onLoadMore() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String,String> map = new HashMap<>();
                map.put("msgType", Constant.QUERY_ALBUM_PROGRESSIVELY);
                final String range = (dataList==null||dataList.isEmpty())?"now":dataList.get(dataList.size()-1).get("moments_id");
                map.put("range", range);
                map.put("mode", isSelfAlbum?"1":"0");
                map.put("user_id", getIntent().getExtras().getString("user_id"));
                //如果是好友
                if(!isSelfAlbum) map.put("self_id", Preference.userInfoMap.get("user_id"));

                final String result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(), 0);
                MainHandler.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        //加载本地数据
                        List<Map<String,String>> list = DataBaseUtil.queryAlbumProgressively(range, getIntent().getExtras().getString("user_id"));
                        //加载更多如果没有内容不触发onLoadDataResult  注意：网络连接失败也走这条路线
                        if(!result.equals("[null]")) {
                            onLoadDataResult(result, true, list);
                            xRecyclerView.loadMoreComplete();
                        }
                        else xRecyclerView.setNoMore(true);

                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    private void refreshThread(final boolean needRefreshAnim){
        new Thread(new Runnable() {
            @Override
            public void run() {
                loadSelfFavoriteInfo();
                Map<String,String> map = new HashMap<>();
                map.put("msgType", Constant.QUERY_ALBUM_REFRESHED);
                final String range = (dataList==null||dataList.isEmpty())?"0":dataList.get(dataList.size()-1).get("moments_id");
                map.put("range", range);
                map.put("mode", isSelfAlbum?"1":"0");
                map.put("user_id", getIntent().getExtras().getString("user_id"));
                //如果是好友
                if(!isSelfAlbum) map.put("self_id", Preference.userInfoMap.get("user_id"));

                final String result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(), 0);

                MainHandler.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        //加载本地数据
                        dataList.clear();
                        List<Map<String,String>> localData = DataBaseUtil.queryAlbumRefreshed(range, getIntent().getExtras().getString("user_id"));
                        for(Map<String,String> temp:localData)
                            dataList.add(temp);

                        onLoadDataResult(result, false, null);
                        if(needRefreshAnim)
                            xRecyclerView.refreshComplete();
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }


    /**
     * 加载一批量获得的信息的图片 累计五张加载失败便直接退出
     */
    private void loadAndSaveImage(List<Map<String,String>> tempData){
        int errorCount = 0;
        if(tempData == null || tempData.isEmpty()) return;
        for(Map<String,String> map:tempData){
            String picPaths = map.get("pictures_id");
            if(picPaths.length()>0){
                String[] pictureImage = picPaths.split("<#>");
                for(String s:pictureImage){
                    if(!ImageTransmissionUtil.loadMomentsSculptureToLocal(this,s)) errorCount++;
                    if(errorCount>=5) return;
                }
            }
        }
        //图片加载完成再次刷新界面
        MainHandler.getInstance().post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }


    /**
     * 接收数据完成
     * @param result:服务器返回数据
     * @param isContinue:是加载更多模式
     * @param localContinueData:本地的加载更多出的数据
     */
    private void onLoadDataResult(String result, boolean isContinue, List<Map<String,String>> localContinueData) {
        if (!result.equals(Constant.SERVER_CONNECTION_ERROR)) {
            if (!result.equals("[null]")){
                final List<Map<String,String>> serverData = FastJSON.parseJSON2ListString(result);
                if(isContinue){
                    LocalDataIOUtil.syncLocalData(SQLLiteConstant.MOMENTS_TABLE, dataList, serverData, localContinueData, true);
                }
                else
                    LocalDataIOUtil.syncLocalData(SQLLiteConstant.MOMENTS_TABLE, dataList, serverData, null, false);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        loadAndSaveImage(serverData);
                    }
                }).start();
            }

            //也许数据发生变化原有数据都被删除了 这种情况只有初次加载或者刷新时会出现
            else
                LocalDataIOUtil.syncLocalData(SQLLiteConstant.MOMENTS_TABLE, dataList, new ArrayList<Map<String, String>>(), null, false);
        }
    }


    /**
     * 接收数据完成
     * @param result:服务器返回数据
     */
    private void onLoadFavoriteDataResult(String result, List<Map<String,String>> localData) {
        if (!result.equals(Constant.SERVER_CONNECTION_ERROR)) {
            if (!result.equals("[null]")){
                final List<Map<String,String>> serverData = FastJSON.parseJSON2ListString(result);
                LocalDataIOUtil.syncLocalData(SQLLiteConstant.FAVORITE_TABLE, localData, serverData, null, false);
            }
            //也许数据发生变化原有数据都被删除了 这种情况只有初次加载或者刷新时会出现
            else
                LocalDataIOUtil.syncLocalData(SQLLiteConstant.FAVORITE_TABLE, localData, new ArrayList<Map<String, String>>(), null, false);
        }
    }

    /**
     * 加载自己喜欢的朋友圈动态
     */
    private void loadSelfFavoriteInfo(){
        Map<String,String> map = new HashMap<>();
        map.put("msgType", Constant.QUERY_SELF_FAVORITE);
        map.put("user_id", Preference.userInfoMap.get("user_id"));
        final String result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(), 0);

        MainHandler.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onLoadFavoriteDataResult(result, favoriteList);
            }
        });
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode != RESULT_OK) return;

        switch (requestCode){
            case ImageSelectorActivity.REQUEST_IMAGE:
                ArrayList<String> result = (ArrayList<String>)data.getSerializableExtra(ImageSelectorActivity.REQUEST_OUTPUT);
                String[] pathPiece = result.get(0).split("\\.");
                backgroundPath = FileHelper.getDiskCacheDir(this)+"/background_"+Preference.userInfoMap.get("user_id")+TimeTools.generateNumberByTime()+"."+pathPiece[pathPiece.length-1];
                FileHelper.copyFile(new File(result.get(0)), backgroundPath);

            case TAKE_PHOTO:
                Glide.with(this).load(backgroundPath).into(momentsBackground);
                final String path = "background/"+backgroundPath.split("background_")[1];
                final Map<String,String> map = new HashMap<>();
                map.put("msgType", Constant.UPLOAD_PICTURE);
                map.put("picPath", path);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String pictureResult = NetConnectionUtil.uploadPicture(JSONObject.fromObject(map).toString(), ImageTools.BitmapToBytes(ImageTools.getLocalBitmap(backgroundPath)));
                        if(pictureResult == null || pictureResult.length()==0 || pictureResult.equals(Constant.SERVER_CONNECTION_ERROR))
                            MainHandler.getInstance().post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(PersonAlbumActivity.this, "抱歉，相册封面上传失败。", Toast.LENGTH_SHORT).show();
                                }
                            });

                        else
                        {
                            Map<String,String> uploadMap = new HashMap<>();
                            uploadMap.put("msgType", Constant.UPDATE_USER);
                            uploadMap.put("mode","9");
                            uploadMap.put("userID", Preference.userInfoMap.get("user_id"));
                            uploadMap.put("moments_background", path);
                            String finalResult = NetConnectionUtil.uploadData(JSONObject.fromObject(uploadMap).toString(), 0);
                            if(!finalResult.equals(Constant.SERVER_CONNECTION_ERROR) && !finalResult.equals("[null]")){
                                Preference.userInfoMap.put("moments_background", path);
                                SharedPreferences.Editor editor = getSharedPreferences("userInfo", MODE_PRIVATE).edit();
                                editor.putString("userInfo",JSONObject.fromObject(Preference.userInfoMap).toString());
                                editor.apply();
                            }
                            else{
                                MainHandler.getInstance().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(PersonAlbumActivity.this, "抱歉，相册封面上传失败。", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }
                }).start();

                break;
        }
    }

    //动态申请权限并且调用选照片
    private void requestPermissionAndPickPhoto(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        else
            ImageSelectorActivity.start(this, 1, ImageSelectorActivity.MODE_SINGLE, false, true, false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if(grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                    ImageSelectorActivity.start(this, 1, ImageSelectorActivity.MODE_SINGLE, false, true, false);
                }
                break;

            default:break;
        }
    }

    private void takePhotos(){
        Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //下面这句指定调用相机拍照后的照片存储的路径
        backgroundPath = FileHelper.getDiskCacheDir(this)+ "/background_"+Preference.userInfoMap.get("user_id")+TimeTools.generateNumberByTime()+".jpg";
        takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(backgroundPath)));
        startActivityForResult(takeIntent, TAKE_PHOTO);
    }
}
