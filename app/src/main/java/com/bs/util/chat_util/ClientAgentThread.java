package com.bs.util.chat_util;

/**
 * Created by 13273 on 2017/10/22.
 *
 */
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.bs.database.DataBaseUtil;
import com.bs.easy_chat.R;
import com.bs.main.MainActivity;
import com.bs.main.chat.ChatActivity;
import com.bs.parameter.ChatServerConstant;
import com.bs.parameter.Constant;
import com.bs.parameter.Preference;
import com.bs.parameter.SQLLiteConstant;
import com.bs.tool_package.ExpressParse;
import com.bs.tool_package.FastJSON;
import com.bs.util.ActivityListUtil;
import com.bs.util.MainHandler;
import com.bs.util.NetConnectionUtil;

import net.sf.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 启动聊天服务的线程（用于接收服务器消息）
 */
public class ClientAgentThread extends Thread{

    private OnChatActivityChangedListener chatActivityChangedListener;
    private OnMainActivityChangedListener mainActivityChangedListener;
    private OnRequestAddFriendListener requestAddFriendListener;
    private OnFriendChangedListener friendChangedListener;
    private OnAgreeAddFriendListener agreeAddFriendListener;
    private OnNewAddFriendRequestListener newAddFriendRequestListener;
    private OnDeleteFriendOKListener deleteFriendOKListener;

    private Context context;
    private Socket sc;
    private InputStream in;
    public OutputStream out;
    private boolean continueRunThread = true;

    /**
     * 连接到服务器
     */
    private void connectToServer() throws Exception{
        //发送Socket连接请求 允许连接1秒
        sc=new Socket();
        SocketAddress address = new InetSocketAddress(Preference.chatServer, 11100);
        sc.connect(address, 1000);
        in=sc.getInputStream();
        out=sc.getOutputStream();

        //直接发送上线请求
        Map<String,String> loginMap = new HashMap<>();
        loginMap.put("msgType", ChatServerConstant.USER_LOGIN);
        loginMap.put("user_id", Preference.userInfoMap.get("user_id"));
        IOUtilCommonSocket.writeString(JSONObject.fromObject(loginMap).toString(), out);
    }

    private void disConnect(){
        try {
            if(in != null)in.close();
            if(out != null)out.close();
            if(sc != null)sc.close();
        } catch (IOException e) {e.printStackTrace();}
    }

    public void stopThread(){
        continueRunThread = false;
    }

    public void setContext(Context context){
        this.context = context;
    }

    @Override
    public void run(){
        try {
            connectToServer();//发送上线请求
        }catch (Exception e){
            Log.d("error", "connect to server failure");
        }
        listenOnServer();
    }

    private void listenOnServer(){
        try {
            while(continueRunThread)
            {
                //接收长度
                int length=IOUtilCommonSocket.readIntNI(in);
                //读取此次的数据字节
                byte[] data=IOUtilCommonSocket.readBytes(in,length);
                //取前四个字节看类型
                byte[] tb={data[0],data[1],data[2],data[3]};
                int type=ConvertUtilCommonSocket.fromBytesToInt(tb);
                //取出数据字节
                byte[] realData=Arrays.copyOfRange(data, 4,data.length);
                Log.d("nettyData", ConvertUtilCommonSocket.fromBytesToString(realData));

                switch(type)
                {
                    case 0://字符串
                        final Map<String,String> infoFromNetty = FastJSON.parseJSON2MapString(ConvertUtilCommonSocket.fromBytesToString(realData));
                        switch (infoFromNetty.get("msgType"))
                        {
                            /**
                             * 用户登录之后的操作
                             */
                            case ChatServerConstant.LOGIN_SUCCEED:
                                 MainHandler.getInstance().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        //加载离线期间好友申请
                                        final List<Map<String,String>> addFriendRequestData = FastJSON.parseJSON2ListString(infoFromNetty.get("add_friend_request"));
                                        DataBaseUtil.createOrOpenDataBase(SQLLiteConstant.FRIEND_REQUEST);
                                        for(Map<String,String> perAddFriendData : addFriendRequestData){
                                            DataBaseUtil.deleteContinuously("delete from friend_request where record_id = '"+perAddFriendData.get("record_id")+"'");
                                            DataBaseUtil.insertContinuously
                                                    (
                                                            "insert into friend_request values('"+perAddFriendData.get("record_id")+"','"+perAddFriendData.get("user_id")+"','"
                                                                    +perAddFriendData.get("request_user")+"','"+perAddFriendData.get("reason")+"','"+perAddFriendData.get("state")+"','0')"
                                                    );
                                        }
                                        DataBaseUtil.closeDatabase();
                                        DataBaseUtil.createOrOpenDataBase(SQLLiteConstant.PROTENTIAL_FRIEND);
                                        for(Map<String,String> perProtentialFriendData : addFriendRequestData){
                                            DataBaseUtil.deleteContinuously("delete from protential_friend where user_id = '"+perProtentialFriendData.get("user_id")+"'");
                                            DataBaseUtil.insertContinuously("insert into protential_friend values('"+perProtentialFriendData.get("user_id")+"','"+perProtentialFriendData.get("gender")+
                                                    "','"+perProtentialFriendData.get("sculpture")+"','"+perProtentialFriendData.get("nickname")+"','"+perProtentialFriendData.get("areas")+"','"+perProtentialFriendData.get("self_introduction")+"','"+perProtentialFriendData.get("handwriting")+"')");
                                        }
                                        DataBaseUtil.closeDatabase();

                                        //加载离线期间好友消息
                                        final List<Map<String,String>> chatData = FastJSON.parseJSON2ListString(infoFromNetty.get("chat_content"));
                                        DataBaseUtil.createOrOpenDataBase(SQLLiteConstant.CHAT_TABLE);
                                        for (Map<String,String> perChatData : chatData){
                                            DataBaseUtil.insertContinuously
                                            (
                                                "insert into chat values('"+perChatData.get("record_id")+"','"+perChatData.get("send_user")+"','"+
                                                perChatData.get("receive_user")+"','"+perChatData.get("time")+"','"+perChatData.get("type")+"','"+perChatData.get("content")+"','"+"0','1')"
                                            );
                                        }
                                        DataBaseUtil.closeDatabase();

                                        //告诉服务器已经收到离线消息
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if(!chatData.isEmpty()){
                                                    Map<String,String> revChatContentResult = new HashMap<>();
                                                    revChatContentResult.put("user_id", Preference.userInfoMap.get("user_id"));
                                                    revChatContentResult.put("msgType", ChatServerConstant.OFFLINE_CHAT_CONTENT_RECEIVE_RESULT);
                                                    NetConnectionUtil.uploadChatData(JSONObject.fromObject(revChatContentResult).toString());
                                                }
                                                if(!addFriendRequestData.isEmpty()){
                                                    Map<String,String> revAddFriendRequestResult = new HashMap<>();
                                                    revAddFriendRequestResult.put("request_user", Preference.userInfoMap.get("user_id"));
                                                    revAddFriendRequestResult.put("msgType", ChatServerConstant.OFFLINE_ADD_FRIEND_RECEIVE_RESULT);
                                                    NetConnectionUtil.uploadChatData(JSONObject.fromObject(revAddFriendRequestResult).toString());
                                                }

                                            }
                                        }).start();

                                        Preference.messageSpinning = false;
                                        if(mainActivityChangedListener != null)
                                            mainActivityChangedListener.onMainActivityChanged(infoFromNetty);
                                    }
                                });

                                break;



                            /**
                             * 用户聊天时操作
                             */
                            case ChatServerConstant.SEND_MESSAGE_RESULT:
                                MainHandler.getInstance().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        DataBaseUtil.update("update chat set state = '"+infoFromNetty.get("state")+"',time = '"+infoFromNetty.get("time")+ "' where record_id = '"+infoFromNetty.get("marking")+"'", SQLLiteConstant.CHAT_TABLE);
                                        if(chatActivityChangedListener != null)
                                            chatActivityChangedListener.onChatActivityChanged(infoFromNetty);
                                    }
                                });
                                break;

                            case ChatServerConstant.RECEIVE_MESSAGE:
                                //告诉服务器已经收到消息
                                Map<String,String> map = new HashMap<>();
                                map.put("msgType", ChatServerConstant.RECEIVE_MESSAGE_RESULT);
                                map.put("record_id", infoFromNetty.get("record_id"));
                                NetConnectionUtil.uploadChatData(JSONObject.fromObject(map).toString());
                                MainHandler.getInstance().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        DataBaseUtil.insert
                                        (
                                            "insert into chat values('"+infoFromNetty.get("record_id")+"','"+infoFromNetty.get("send_user")+"','"+
                                            infoFromNetty.get("receive_user")+"','"+infoFromNetty.get("time")+"','"+infoFromNetty.get("type")+"','"+infoFromNetty.get("content")+"','"+"0','1')",
                                            SQLLiteConstant.CHAT_TABLE
                                        );
                                        HashMap<String,String> msg = new HashMap<>();
                                        msg.put("msgType", ChatServerConstant.RECEIVE_MESSAGE);
                                        msg.put("record_id", infoFromNetty.get("record_id"));
                                        msg.put("send_user", infoFromNetty.get("send_user"));
                                        msg.put("receive_user", infoFromNetty.get("receive_user"));
                                        msg.put("time", infoFromNetty.get("time"));
                                        msg.put("type", infoFromNetty.get("type"));
                                        msg.put("content", infoFromNetty.get("content"));
                                        msg.put("is_send", "0");
                                        msg.put("state", "1");
                                        if(chatActivityChangedListener != null)
                                            chatActivityChangedListener.onChatActivityChanged(msg);

                                        if(mainActivityChangedListener != null)
                                            mainActivityChangedListener.onMainActivityChanged(msg);

                                        newMessagesNotification(infoFromNetty);
                                    }
                                });
                                break;



                            /**
                             * 请求添加好友时操作（需要即使通知被请求人）
                             */
                            case ChatServerConstant.REQUEST_ADD_FRIEND_RESULT:
                                MainHandler.getInstance().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        DataBaseUtil.insert
                                            (
                                                "insert into friend_request values('"+infoFromNetty.get("record_id")+"','"+infoFromNetty.get("user_id")+"','"
                                                 +infoFromNetty.get("request_user")+"','"+infoFromNetty.get("reason")+"','"+infoFromNetty.get("state")+"','1')", SQLLiteConstant.FRIEND_REQUEST
                                            );
                                        if(requestAddFriendListener != null)
                                            requestAddFriendListener.onRequestAddFriend(infoFromNetty);
                                    }
                                });
                                break;

                            case ChatServerConstant.RECEIVE_ADD_FRIEND:
                                //告诉服务器已经收到消息
                                Map<String,String> map0 = new HashMap<>();
                                map0.put("msgType", ChatServerConstant.RECEIVE_ADD_FRIEND_RESULT);
                                map0.put("record_id", infoFromNetty.get("record_id"));
                                NetConnectionUtil.uploadChatData(JSONObject.fromObject(map0).toString());
                                MainHandler.getInstance().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        DataBaseUtil.insert(
                                            "insert into friend_request values('"+infoFromNetty.get("record_id")+"','"+infoFromNetty.get("user_id")+"','"
                                             +infoFromNetty.get("request_user")+"','"+infoFromNetty.get("reason")+"','"+infoFromNetty.get("state")+"','0')", SQLLiteConstant.FRIEND_REQUEST
                                        );

                                        DataBaseUtil.delete("delete from protential_friend where user_id = '"+infoFromNetty.get("user_id")+"'", SQLLiteConstant.PROTENTIAL_FRIEND);

                                        DataBaseUtil.insert("insert into protential_friend values('"+infoFromNetty.get("user_id")+"','"+infoFromNetty.get("gender")+
                                                "','"+infoFromNetty.get("sculpture")+"','"+infoFromNetty.get("nickname")+"','"+infoFromNetty.get("areas")+"','"+infoFromNetty.get("self_introduction")+"','"+infoFromNetty.get("handwriting")+"')",SQLLiteConstant.PROTENTIAL_FRIEND
                                        );

                                        if(newAddFriendRequestListener != null)
                                            newAddFriendRequestListener.onNewAddFriend();
                                    }
                                });
                                break;


                            /**
                             * 好友变更时操作
                             */
                            case ChatServerConstant.RECEIVE_NEW_FRIEND:
                                if(MainActivity.mainActivity != null)
                                    MainActivity.mainActivity.queryFriends();
                                MainHandler.getInstance().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(friendChangedListener != null)
                                            friendChangedListener.onFriendChanged();
                                        //若自己是被请求人
                                        if(infoFromNetty.get("request_user").equals(Preference.userInfoMap.get("user_id")))
                                        {
                                            //聊天记录里多出我同意了你的好友申请
                                            HashMap<String,String> msg = new HashMap<>();
                                            msg.put("msgType", ChatServerConstant.RECEIVE_MESSAGE);
                                            msg.put("record_id", infoFromNetty.get("record_id"));
                                            msg.put("send_user", infoFromNetty.get("request_user"));
                                            msg.put("receive_user", infoFromNetty.get("user_id"));
                                            msg.put("time", infoFromNetty.get("time"));
                                            msg.put("type", "text");
                                            msg.put("content", "我通过了你的好友请求，现在我们可以开始聊天啦。");
                                            msg.put("is_send", "1");
                                            msg.put("state", "1");
                                            msg.put("result", Constant.OPERATION_SUCCEED);
                                            DataBaseUtil.insert
                                            (
                                                "insert into chat values('"+msg.get("record_id")+"','"+msg.get("send_user")+"','"+
                                                        msg.get("receive_user")+"','"+msg.get("time")+"','"+msg.get("type")+"','"+msg.get("content")+"','"+"1','1')",
                                                SQLLiteConstant.CHAT_TABLE
                                            );
                                            if(agreeAddFriendListener != null)
                                                agreeAddFriendListener.onAgreeAddFriend(msg);
                                        }
                                        //更新本地数据库信息
                                        DataBaseUtil.update("update friend_request set state = 'pass' where user_id = '"+infoFromNetty.get("user_id")+"'and request_user = '"+infoFromNetty.get("request_user")+"'", SQLLiteConstant.FRIEND_REQUEST);
                                    }
                                });
                                break;


                            case ChatServerConstant.DELETE_FRIEND_RESULT:
                                if(MainActivity.mainActivity != null)
                                    MainActivity.mainActivity.queryFriends();
                                MainHandler.getInstance().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(friendChangedListener != null)
                                            friendChangedListener.onFriendChanged();

                                        if(deleteFriendOKListener != null)
                                            deleteFriendOKListener.onDeleteFriendOK(infoFromNetty);
                                    }
                                });
                                break;
                        }
                        break;
                }
            }
        } catch (Exception e1) {
            //出现异常 从服务器断开连接
            Log.d("error", "listen on port failure");
            Preference.messageSpinning = true;
            if(mainActivityChangedListener != null){
                final Map<String,String> map = new HashMap<>();
                map.put("msgType", ChatServerConstant.CONNECTING);
                MainHandler.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                    mainActivityChangedListener.onMainActivityChanged(map);
                    }
                });
            }
            disConnect();
            //重新连接（如果还需要的话）
            try{
                Thread.sleep(5000);//休息5秒继续连接
                connectToServer();
            }catch (Exception e2){
                Log.d("error", "connect to server failure");
            }
            if(continueRunThread)
                listenOnServer();
        }
    }


    /**
     * 新消息推送
     */
    private void newMessagesNotification(Map<String, String> info){
        if(ActivityListUtil.appIsRunningForeground() || !Preference.isNotified) return;
        String text = "";
        NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.logo)
                .setContentText("点击查看")
                .setContentTitle(Preference.isShownDetail ? text = generateInfoDetail(info):"你收到了一条消息")
                .setWhen(System.currentTimeMillis());

        ActivityListUtil.destroySpecialActivity("com.bs.main.chat.ChatActivity");
        Map<String,String> friendInfo = DataBaseUtil.queryFriendsExactly(info.get("send_user")).get(0);
        Bundle bundle = new Bundle();
        bundle.putString("name", text.split(":")[0]);
        bundle.putString("friend_id", info.get("send_user"));
        bundle.putString("sculpture", friendInfo.get("sculpture"));
        Intent intent = new Intent(context, ChatActivity.class).putExtras(bundle);
        PendingIntent pi = PendingIntent.getActivity(context , 0, intent, 0);
        builder.setContentIntent(pi);
        nManager.notify(0 , builder.build());
    }

    private String generateInfoDetail(Map<String,String> info){
        Map<String,String> friendInfo = DataBaseUtil.queryFriendsExactly(info.get("send_user")).get(0);
        String user,content = ":";
        if(friendInfo.isEmpty()) user = info.get("send_user");
        else user = friendInfo.get("remark_name").length() == 0 ? friendInfo.get("nickname") : friendInfo.get("remark_name");
        switch (info.get("type")){
            case "text":
                content += info.get("content");
                break;

            case "emoji":
                content += ExpressParse.parseExpress(info.get("content"));
                break;

            case "picture":
                content += "[图片]";
                break;
        }
        return user + content;
    }


    /**
     * 设置接收到Netty服务器传回数据的回调
     */
    public interface OnChatActivityChangedListener{
        void onChatActivityChanged(Map<String,String> msg);
    }
    public void setOnChatActivityChangedListener(OnChatActivityChangedListener listener){
        chatActivityChangedListener = listener;
    }


    /**
     * 设置网络连接状态的监听
     */
    public interface OnMainActivityChangedListener{
        void onMainActivityChanged(Map<String,String> msg);
    }
    public void setOnMainActivityChangedListener(OnMainActivityChangedListener listener){
        mainActivityChangedListener = listener;
    }


    /**
     * 设置发送好友请求的监听
     */
    interface OnRequestAddFriendListener{
        void onRequestAddFriend(Map<String,String> msg);
    }
    public void setOnRequestAddFriendListener(OnRequestAddFriendListener listener){
        requestAddFriendListener = listener;
    }


    /**
     * 设置添加新好友的接口回调
     */
    public interface OnFriendChangedListener{
        void onFriendChanged();
    }
    public void setOnAddNewFriendListener(OnFriendChangedListener listener){
        friendChangedListener = listener;
    }


    /**
     * 同意好友申请结果监听
     */
    interface OnAgreeAddFriendListener{
        void onAgreeAddFriend(Map<String,String> msg);
    }

    public void setOnAgreeAddFriendListener(OnAgreeAddFriendListener listener){
        agreeAddFriendListener = listener;
    }


    /**
     * 刷新好友添加请求界面
     */
    interface OnNewAddFriendRequestListener{
        void onNewAddFriend();
    }

    public void setOnNewAddFriendRequestListener(OnNewAddFriendRequestListener listener){
        newAddFriendRequestListener = listener;
    }


    /**
     * 删除完成监听器
     */
    interface OnDeleteFriendOKListener{
        void onDeleteFriendOK(Map<String,String> msg);
    }
    public void setOnDeleteFriendOKListener(OnDeleteFriendOKListener listener){
        deleteFriendOKListener = listener;
    }

}
