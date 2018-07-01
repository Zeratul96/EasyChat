package com.bs.main;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.bs.database.DataBaseUtil;
import com.bs.easy_chat.R;
import com.bs.parameter.ChatServerConstant;
import com.bs.service.MainService;
import com.bs.parameter.Constant;
import com.bs.parameter.Preference;
import com.bs.parameter.SQLLiteConstant;
import com.bs.tool_package.FastJSON;
import com.bs.util.BaseActivity;
import com.bs.util.ImageTransmissionUtil;
import com.bs.util.LocalDataIOUtil;
import com.bs.util.MainHandler;
import com.bs.util.NetConnectionUtil;
import com.bs.util.chat_util.ClientAgentThread;
import com.bs.welcome.LoginActivity;

import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Intent:autoLogin
 */

public class MainActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener,ClientAgentThread.OnMainActivityChangedListener{

    public MessageFragment messageFragment;
    public ContactFragment contactFragment;
    private FunctionFragment functionFragment;
    private PersonFragment personFragment;
    private Thread loginCheckThread;
    private boolean isReconnecting;

    public static MainActivity mainActivity;

    /**
     * 自动登录进行额外的身份验证
     */
    private void loginCheck(){
        loginCheckThread = new Thread(new Runnable() {
            @Override
            public void run() {
                final Map<String, String> map = new HashMap<>();
                final SharedPreferences sp = getSharedPreferences("loginInfo", MODE_PRIVATE);
                map.put("msgType", Constant.LOGIN_CHECK);
                map.put("userID", sp.getString("userID", ""));
                String result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(),0);
                if(result.equals(Constant.SERVER_CONNECTION_ERROR)){
                    //连接失败继续尝试连接
                    isReconnecting = true;
                    try{
                        while (isReconnecting){
                            result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(), 0);
                            if(result.equals(Constant.SERVER_CONNECTION_ERROR)) Thread.sleep(5000);

                            else isReconnecting = false;
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                Preference.userInfoMap = FastJSON.parseJSON2ListString(result).get(0);

                MainHandler.getInstance().post(new Runnable() {
                    @Override
                    public void run() {

                        if(Preference.userInfoMap.get("password").equals(sp.getString("password", ""))){
                            if(Preference.userInfoMap.get("banning").equals("0"))
                                showAlertDialog("抱歉，该账号已被禁封。");
                            else{
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loginSucceed();
                                    }
                                }).start();

                                SharedPreferences.Editor editor = getSharedPreferences("userInfo", MODE_PRIVATE).edit();
                                editor.putString("userInfo", JSONObject.fromObject(Preference.userInfoMap).toString());
                                editor.apply();
                            }
                        }
                        else
                            showAlertDialog("身份信息已过期，请返回登录界面重新登陆。");
                    }
                });
            }
        });
        loginCheckThread.start();
    }

    /**
     * 查询朋友信息
     */
    public void queryFriends(){
        HashMap<String,String> map = new HashMap<>();
        map.put("msgType", Constant.QUERY_FRIENDS);
        map.put("userID", Preference.userInfoMap.get("user_id"));
        String result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(), 0);
        List<Map<String,String>> mData = DataBaseUtil.queryFriends();
        if(!result.equals(Constant.SERVER_CONNECTION_ERROR) && !result.equals("[null]")) {
            ArrayList<Map<String,String>> serverData = (ArrayList<Map<String,String>>) FastJSON.parseJSON2ListString(result);
            LocalDataIOUtil.syncLocalData(SQLLiteConstant.FRIEND_TABLE, mData, serverData, null, false);//同步本地数据库
            loadAndSaveSculptures(mData);
        }
        else if(result.equals("[null]"))
            LocalDataIOUtil.syncLocalData(SQLLiteConstant.FRIEND_TABLE, mData, new ArrayList<Map<String, String>>(), null, false);
    }

    /**
     * 加载所有好友的头像 累计五张加载失败便直接退出
     */
    private void loadAndSaveSculptures(List<Map<String,String>> tempData){
        int errorCount = 0, loopCount = 0;
        if(tempData == null || tempData.isEmpty()) return;
        for(Map<String,String> map:tempData){
            loopCount++;
            String picPaths = map.get("sculpture");
            if(picPaths.length()>0){
                if(!ImageTransmissionUtil.loadSculptureToLocal(this, picPaths)) errorCount++;
                if(errorCount >= 5) {
                    MainHandler.getInstance().post(new Runnable() {
                        @Override
                        public void run() {if(contactFragment != null) contactFragment.contactAdapter.notifyDataSetChanged();}
                    });
                    return;
                }
            }

            if(loopCount % 10 == 0) MainHandler.getInstance().post(new Runnable() {
                @Override
                public void run() {if(contactFragment != null) contactFragment.contactAdapter.notifyDataSetChanged();}
            });
        }
        if(loopCount % 10 != 0) MainHandler.getInstance().post(new Runnable() {
            @Override
            public void run() {if(contactFragment != null) contactFragment.contactAdapter.notifyDataSetChanged();}
        });
    }

    //登录成功
    //加载个人头像
    //启动后台 连接Netty服务器
    private void loginSucceed(){
        ImageTransmissionUtil.loadSculptureToLocal(this, Preference.userInfoMap.get("sculpture"));//加载自己头像到本地
        queryFriends();
        //启动后台 连接聊天服务器 （包括接收离线期间的消息等）
        startService(new Intent(MainActivity.this, MainService.class));
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        mainActivity = this;

        //设置聊天线程监听 聊天线程在这里被创建
        NetConnectionUtil.cat = new ClientAgentThread();
        NetConnectionUtil.cat.setOnMainActivityChangedListener(this);
        ((RadioGroup)findViewById(R.id.main_bottom_tabs)).setOnCheckedChangeListener(this);
        ((RadioButton)findViewById(R.id.message)).setChecked(true);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        if(getIntent().getExtras().getBoolean("autoLogin")) {
            //预先加载好用户信息
            Preference.userInfoMap = FastJSON.parseJSON2MapString(getSharedPreferences("userInfo", MODE_PRIVATE).getString("userInfo", ""));
            loginCheck();
        }
        else new Thread(new Runnable() {
                @Override
                public void run() {
                    loginSucceed();
                }
            }).start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadingSettings();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //结束登录账号线程
        if(isReconnecting) {
            isReconnecting = false;
            loginCheckThread.interrupt();
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        switch (i){
            case R.id.message: changeFragment(0);break;

            case R.id.contact: changeFragment(1);break;

            case R.id.function: changeFragment(2);break;

            case R.id.person: changeFragment(3);break;
        }
    }

    /**
     * 拿到fragmentManager 开始执行Fragment的添加、移除、替换、隐藏四种操作
     * Fragment显示于主界面的Main_Content中
     * 注意：管理结束后要commit才能使当前Activity的Fragment生效
     */
    private void changeFragment(int num){
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        hideFragment(fragmentTransaction);
        switch(num){
            case 0:
                if (messageFragment == null){
                    messageFragment = new MessageFragment();
                    fragmentTransaction.add(R.id.main_content, messageFragment);
                }
                else {
                    fragmentTransaction.show(messageFragment);
                    messageFragment.setSpinnerState(Preference.messageSpinning);
                }
                break;

            case 1:
                if (contactFragment == null ){
                    contactFragment = new ContactFragment();
                    fragmentTransaction.add(R.id.main_content, contactFragment);
                }
                else{
                    fragmentTransaction.show(contactFragment);
                    contactFragment.cancelEditFocusAndResetUI();
                }
                break;

            case 2:
                if (functionFragment == null){
                    functionFragment = new FunctionFragment();
                    fragmentTransaction.add(R.id.main_content, functionFragment);
                }
                else
                    fragmentTransaction.show(functionFragment);

                break;

            case 3:
                if (personFragment == null){
                    personFragment = new PersonFragment();
                    fragmentTransaction.add(R.id.main_content, personFragment);
                }
                else{
                   fragmentTransaction.show(personFragment);
                    personFragment.loadSculpture();
                }
                break;
        }
        fragmentTransaction.commit();
    }


    //隐藏所有现有的Fragment
    private void hideFragment(FragmentTransaction fragmentTransaction){
        if(messageFragment != null)
            fragmentTransaction.hide(messageFragment);
        if(contactFragment != null)
            fragmentTransaction.hide(contactFragment);
        if(functionFragment != null)
            fragmentTransaction.hide(functionFragment);
        if(personFragment != null)
            fragmentTransaction.hide(personFragment);
    }

    private void showAlertDialog(String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.AlertDialogCustom);builder.setTitle("登录提示");
        //对话框内容
        builder.setMessage(msg);
        //确定按钮
        builder.setPositiveButton("重新登录", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                MainActivity.this.finish();
            }
        });
        //固定对话框使其不可被取消
        builder.setCancelable(false);

        //创建对话框
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void loadingSettings(){
        SharedPreferences sp = getSharedPreferences("settingsInfo",MODE_PRIVATE);
        Preference.isNotified = sp.getBoolean("notification",true);
        Preference.isShownDetail = sp.getBoolean("detail",true);
        Preference.isDraft = sp.getBoolean("draft",true);
        Preference.isSync = sp.getBoolean("sync",false);
        Preference.isTimeLineStyle = sp.getBoolean("timeline",false);
    }

    @Override
    public void onMainActivityChanged(final Map<String,String> msg) {
        switch (msg.get("msgType")){
            case ChatServerConstant.LOGIN_SUCCEED:
                if(messageFragment != null){
                    messageFragment.setSpinnerState(Preference.messageSpinning);
                    messageFragment.pickUpNecessaryInfoAndRefreshUI(DataBaseUtil.queryMessage());
                }
                break;

            case ChatServerConstant.CONNECTING:
                if(messageFragment != null){
                    messageFragment.setSpinnerState(Preference.messageSpinning);
                }
                break;

            case ChatServerConstant.RECEIVE_MESSAGE:
                if(messageFragment != null)
                    messageFragment.onReceiveSingleMessage(msg, true);
                break;
        }
    }
}
