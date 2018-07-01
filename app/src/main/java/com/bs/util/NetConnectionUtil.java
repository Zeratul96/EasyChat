package com.bs.util;

import com.bs.parameter.Constant;
import com.bs.parameter.Preference;
import com.bs.tool_package.MyConverter;
import com.bs.util.chat_util.ClientAgentThread;
import com.bs.util.chat_util.IOUtilCommonSocket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by 13273 on 2017/9/16.
 *
 */

public class NetConnectionUtil {

    private static Socket ss = null;
    private static DataInputStream din = null;
    private static DataOutputStream dout = null;
    public static ClientAgentThread cat;

    private static void establishConnection(int serverType) throws Exception
    {
        ss = new Socket();
        SocketAddress address = null;

        switch (serverType){
            case Constant.USER_SERVER:
                address = new InetSocketAddress(Preference.serverAddress, 11112);
                break;

            case Constant.PICTURE_SERVER:
                address = new InetSocketAddress(Preference.pictureServer,11000);
                break;
        }

        ss.connect(address, 1000);
        din = new DataInputStream(ss.getInputStream());
        dout = new DataOutputStream(ss.getOutputStream());
    }

    private static void closeConnection()
    {
        if(dout!=null)
        {
            try {
                dout.flush();
                dout.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(din!=null)
        {
            try {
                din.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(ss!=null)
        {
            try {
                ss.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static synchronized String uploadData(String msg, int serverType)
    {
        String result = "";

        try {
            establishConnection(serverType);
            dout.writeUTF(MyConverter.escape(msg));

            int num= din.readInt();
            for(int i=0;i<=num;i++)
                result += din.readUTF();

            result = MyConverter.unescape(result);

        } catch (Exception e) {
            e.printStackTrace();
            result = Constant.SERVER_CONNECTION_ERROR;
        }finally {
            closeConnection();
        }
        return result;
    }


    public static synchronized byte[] downLoadPicture(String msg)
    {
        byte[] picData = null;

        try {
            establishConnection(Constant.PICTURE_SERVER);

            dout.writeUTF(MyConverter.escape(msg));

            picData = IOUtil.readImageBytes(din);

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            closeConnection();
        }

        return picData;
    }

    public static synchronized String uploadPicture(String msg, byte[] picData)
    {
        String result = "";

        try {
            establishConnection(Constant.PICTURE_SERVER);

            dout.writeUTF(MyConverter.escape(msg));

            dout.writeInt(picData.length);

            dout.write(picData);

            result = MyConverter.unescape(din.readUTF());

        } catch (Exception e) {
            result = Constant.SERVER_CONNECTION_ERROR;
            e.printStackTrace();
        }finally {
            closeConnection();
        }

        return result;
    }

    /**
     *
     * @param msg:消息
     * @return 返回是否消息发送成功 返回空字符串表示消息已成功发送至服务器
     * 如果消息成功发送 服务器返回的信息由ChatAgentThread接收
     */
    public static synchronized String uploadChatData(String msg){
        String result = "";
        try {
            IOUtilCommonSocket.writeString(msg, cat.out);
        } catch (Exception e) {
            result = Constant.SERVER_CONNECTION_ERROR;
            e.printStackTrace();
        }
        return result;
    }

}
