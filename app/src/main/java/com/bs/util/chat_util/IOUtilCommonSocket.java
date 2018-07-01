package com.bs.util.chat_util;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by 13273 on 2017/10/22.
 *
 */

public class IOUtilCommonSocket {

    //消息的前四个字节为长度字节数
    //第3个字节为类型编号   0--字符串   1-整数

    public static void writeString(String msg,OutputStream out) throws Exception
    {
        byte[] typeBytes= ConvertUtilCommonSocket.fromIntToBytes(0);
        byte[] data=ConvertUtilCommonSocket.fromStringToBytes(msg);

        out.write(ConvertUtilCommonSocket.fromIntToBytesNI(typeBytes.length+data.length));
        out.write(typeBytes);
        out.write(data);
        out.flush();
    }

    public static void writeInt(int dataInt,OutputStream out) throws Exception
    {
        byte[] typeBytes=ConvertUtilCommonSocket.fromIntToBytes(1);
        byte[] data=ConvertUtilCommonSocket.fromIntToBytes(dataInt);

        out.write(ConvertUtilCommonSocket.fromIntToBytesNI(typeBytes.length+data.length));
        out.write(typeBytes);
        out.write(data);
        out.flush();
    }

    static byte[] readBytes(InputStream din, int count) throws Exception
    {
        byte[] buf=new byte[count];
        int len=count;
        int curr=din.read(buf);
        count=count-curr;
        while(count!=0)
        {
            curr=din.read(buf,len-count,count);
            count=count-curr;
        }
        return buf;
    }

    public static int readInt(InputStream din) throws Exception
    {
        byte[] buf=readBytes(din,4);
        return ConvertUtilCommonSocket.fromBytesToInt(buf);
    }

    static int readIntNI(InputStream din) throws Exception
    {
        byte[] buf=readBytes(din,4);
        return ConvertUtilCommonSocket.fromBytesToIntNI(buf);
    }
}
