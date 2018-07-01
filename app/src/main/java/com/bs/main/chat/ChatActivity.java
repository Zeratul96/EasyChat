package com.bs.main.chat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bs.database.DataBaseUtil;
import com.bs.easy_chat.R;
import com.bs.main.MainActivity;
import com.bs.parameter.ChatServerConstant;
import com.bs.parameter.Constant;
import com.bs.parameter.Preference;
import com.bs.parameter.SQLLiteConstant;
import com.bs.tool_package.FileHelper;
import com.bs.tool_package.ImageTools;
import com.bs.tool_package.TimeTools;
import com.bs.util.BaseActivity;
import com.bs.util.MainHandler;
import com.bs.util.NetConnectionUtil;
import com.bs.util.chat_util.ClientAgentThread;
import com.yongchun.library.view.ImageSelectorActivity;

import net.sf.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Intent:name
 * friend_id(对方ID)
 * sculpture(对方头像)
 * messageDetail
 */
public class ChatActivity extends BaseActivity implements View.OnClickListener, ClientAgentThread.OnChatActivityChangedListener{
    GridView expressView;
    GridView functionView;
    ImageView plusBtn;
    EditText chatContent;
    Button sendBtn;
    boolean functionMenuOn;
    List<Map<String,String>> sendingMsgCache = new ArrayList<>();

    RecyclerView chatRecyclerView;
    ChatAdapter chatAdapter;
    List<Map<String,String>> dataList;

    String takenPhotoPath;
    List<String> selectedPicture = new ArrayList<>();
    final int TAKE_PHOTO = 2;

    private void avoidFileExposure(){
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);
        avoidFileExposure();
        MainActivity.mainActivity.messageFragment.clearUnreadNumber(getIntent().getExtras().getString("friend_id"));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");
        if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ((TextView)findViewById(R.id.title_name)).setText(getIntent().getExtras().getString("name"));

        sendBtn = (Button) findViewById(R.id.send_msg);
        sendBtn.setOnClickListener(this);

        chatRecyclerView = (RecyclerView) findViewById(R.id.chat_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        chatRecyclerView.setLayoutManager(layoutManager);
        ((SimpleItemAnimator)chatRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);


        expressView = (GridView) findViewById(R.id.express_grid_view);
        expressView.setAdapter(new ExpressAdapter(this));
        expressView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                sendMsg("emoji", String.valueOf(position), null);
            }
        });

        functionView = (GridView) findViewById(R.id.function_grid_view);
        functionView.setAdapter(new FunctionAdapter(this));
        functionView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        functionView.setVisibility(View.GONE);
                        expressView.setVisibility(View.VISIBLE);
                        break;

                    case 1:
                        requestPermissionAndPickPhoto();
                        break;

                    case 2:
                        takePhotos();
                        break;
                }
            }
        });

        chatContent = (EditText) findViewById(R.id.text_edit);
        chatContent.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    expressView.setVisibility(View.GONE);
                    functionView.setVisibility(View.GONE);
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(chatContent ,InputMethodManager.SHOW_IMPLICIT);
                    functionMenuOn = false;

                    if(dataList.size() - 1 >= 0)
                        chatRecyclerView.smoothScrollToPosition(dataList.size()-1);
                }
                else{
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                }
            }
        });

        plusBtn = (ImageView) findViewById(R.id.plus_btn);
        plusBtn.setOnClickListener(this);

        NetConnectionUtil.cat.setOnChatActivityChangedListener(this);
    }

    /**
     * 在这里加载相应聊天数据
     */
    @Override
    protected void onStart() {
        super.onStart();
        chatRecyclerView.setAdapter(chatAdapter = new ChatAdapter(
            dataList = getIntent().getSerializableExtra("message_detail") == null ? DataBaseUtil.queryMessageWithFriend(getIntent().getExtras().getString("friend_id")):(ArrayList)getIntent().getSerializableExtra("message_detail"),
            this, getIntent().getExtras().getString("sculpture")
        ));
        if(dataList.size() - 1 >= 0)
            chatRecyclerView.scrollToPosition(dataList.size()-1);
    }

    @Override
    public void onClick(View v) {
        if(v == plusBtn){
            functionMenuOn = !functionMenuOn;
            if(functionMenuOn){
                chatContent.clearFocus();
                expressView.setVisibility(View.GONE);
                functionView.setVisibility(View.VISIBLE);
            }
            else chatContent.requestFocus();

            if(dataList.size() - 1 >= 0)
                chatRecyclerView.smoothScrollToPosition(dataList.size()-1);
        }
        else if(v == sendBtn){
            if(chatContent.getText().toString().length()==0) return;
            sendMsg("text", null, null);
        }
    }

    /**
     *
     * @param msgType:发送消息的种类
     * @param pictureCode:消息中图片代码
     * @param pictureMessageMap:图片消息有这个参数，其他都为null
     */
    private void sendMsg(String msgType, String pictureCode, Map<String,String> pictureMessageMap){
        Map<String,String> messageMap = pictureMessageMap;
        if(msgType.equals("text") || msgType.equals("emoji"))
            messageMap = showMessageOnAdapter(msgType, pictureCode);
        final Map<String,String> pictureMapPointer = messageMap;

        new Thread(new Runnable() {
            @Override
            public void run() {
                final String result = NetConnectionUtil.uploadChatData(JSONObject.fromObject(pictureMapPointer).toString());
                    MainHandler.getInstance().post(new Runnable() {
                        @Override
                        public void run() {
                            if(MainActivity.mainActivity.messageFragment != null)
                                MainActivity.mainActivity.messageFragment.onReceiveSingleMessage(pictureMapPointer, false);

                            if(result.length()>0){//连接服务器失败或者消息没有成功传至服务器
                                pictureMapPointer.put("state", "-1");
                                chatAdapter.notifyItemChanged(dataList.indexOf(pictureMapPointer));
                                DataBaseUtil.insert
                                (
                                    "insert into chat values('"+pictureMapPointer.get("marking")+"','"+pictureMapPointer.get("send_user")+"','"+
                                    pictureMapPointer.get("receive_user")+"','"+pictureMapPointer.get("time")+"','"+pictureMapPointer.get("type")+"','"+pictureMapPointer.get("content")+"','"+"1','-1')",
                                    SQLLiteConstant.CHAT_TABLE
                                );
                            }

                            else{//传到服务器暂时成功 需要进入缓存等待区等待服务器回应
                                sendingMsgCache.add(pictureMapPointer);
                                DataBaseUtil.insert
                                (
                                    "insert into chat values('"+pictureMapPointer.get("marking")+"','"+pictureMapPointer.get("send_user")+"','"+
                                    pictureMapPointer.get("receive_user")+"','"+pictureMapPointer.get("time")+"','"+pictureMapPointer.get("type")+"','"+pictureMapPointer.get("content")+"','"+"1','0')",
                                    SQLLiteConstant.CHAT_TABLE
                                );
                            }
                        }
                    });
            }
        }).start();
    }

    private Map<String,String> showMessageOnAdapter(String msgType, String picture){
        final Map<String,String> messageMap = new HashMap<>();
        messageMap.put("msgType", ChatServerConstant.SEND_MESSAGE);
        messageMap.put("marking", TimeTools.generateNumberByTime()+Preference.userInfoMap.get("user_id"));//信息发送记录标志
        messageMap.put("send_user", Preference.userInfoMap.get("user_id"));//发送者
        messageMap.put("receive_user", getIntent().getExtras().getString("friend_id"));//接收消息者
        messageMap.put("time", TimeTools.generateCustomTime("yyyy-MM-dd-HH-mm"));//时间
        messageMap.put("type", msgType);
        messageMap.put("content", msgType.equals("text")?chatContent.getText().toString():picture);
        messageMap.put("state", "0");

        dataList.add(messageMap);
        chatAdapter.notifyDataSetChanged();
        chatRecyclerView.scrollToPosition(dataList.size()-1);
        if(msgType.equals("text")) chatContent.setText("");

        return messageMap;
    }

    public void onChatActivityChanged(final Map<String, String> msg){
        switch (msg.get("msgType")){
            case ChatServerConstant.SEND_MESSAGE_RESULT:
                //在缓存区找到对应的等待回复发送状态的信息
                Map<String,String> targetMsg = null;
                for(Map<String,String> cache:sendingMsgCache){
                    if(cache.get("marking").equals(msg.get("marking"))){
                        targetMsg = cache;
                        break;
                    }
                }
                sendingMsgCache.remove(targetMsg);
                //找到targetMsg所在adapter中的位置并记录下来 接下来准备改动Map数据
                int position = dataList.indexOf(targetMsg);
                targetMsg.put("state", msg.get("state"));
                chatAdapter.notifyItemChanged(position);
                break;


            case ChatServerConstant.RECEIVE_MESSAGE:
                if(!msg.get("send_user").equals(getIntent().getExtras().getString("friend_id"))) return;
                dataList.add(msg);
                chatAdapter.notifyDataSetChanged();
                chatRecyclerView.scrollToPosition(dataList.size()-1);
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

    private void takePhotos(){
        Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //下面这句指定调用相机拍照后的照片存储的路径
        takenPhotoPath = FileHelper.getDiskCacheDir(this)+ "/chat_"+Preference.userInfoMap.get("user_id")+TimeTools.generateNumberByTime()+".jpg";
        takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(takenPhotoPath)));
        startActivityForResult(takeIntent, TAKE_PHOTO);
    }



    //动态申请权限并且调用选照片
        private void requestPermissionAndPickPhoto(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        else
            ImageSelectorActivity.start(this, 9, ImageSelectorActivity.MODE_MULTIPLE, false, true, false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if(grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                    ImageSelectorActivity.start(this, 9, ImageSelectorActivity.MODE_MULTIPLE, false, true, false);
                }
                break;

            default:break;
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
                    String path = FileHelper.getDiskCacheDir(this)+"/chat_"+Preference.userInfoMap.get("user_id")+TimeTools.generateNumberByTime()+"."+piece[piece.length-1];
                    FileHelper.copyFile(new File(s), path);
                    selectedPicture.add(path);
                }
                onUploadImage();
                break;

            case TAKE_PHOTO:
                selectedPicture.add(takenPhotoPath);
                onUploadImage();
                break;
        }
    }

    private void onUploadImage(){
        final Map<String, String> uploadMap = new HashMap<>();
        uploadMap.put("msgType", Constant.UPLOAD_PICTURE);

        for (final String path:selectedPicture)
        {
            final String serverPath = "chat/"+path.split("chat_")[1];
            final Map<String,String> pictureMap = showMessageOnAdapter("picture", serverPath);//使图片显示在聊天记录里
            uploadMap.put("picPath", serverPath);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final String result = NetConnectionUtil.uploadPicture(JSONObject.fromObject(uploadMap).toString(), ImageTools.BitmapToBytes(ImageTools.getLocalBitmap(path)));
                    if(result == null||result.length()==0||result.equals(Constant.SERVER_CONNECTION_ERROR)){//传输图片失败
                        MainHandler.getInstance().post(new Runnable() {
                            @Override
                            public void run() {
                                pictureMap.put("state", "-1");
                                chatAdapter.notifyItemChanged(dataList.indexOf(pictureMap));
                                DataBaseUtil.insert
                                (
                                    "insert into chat values('"+pictureMap.get("marking")+"','"+pictureMap.get("send_user")+"','"+
                                            pictureMap.get("receive_user")+"','"+pictureMap.get("time")+"','"+pictureMap.get("type")+"','"+pictureMap.get("content")+"','"+"1','-1')",
                                    SQLLiteConstant.CHAT_TABLE
                                );
                            }
                        });
                    }

                    else sendMsg("picture", serverPath, pictureMap);//传输图片成功
                }
            }).start();
        }

        //清除本次上传的图片内容
        selectedPicture.clear();
    }

}
