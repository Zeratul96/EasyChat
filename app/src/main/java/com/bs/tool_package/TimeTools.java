package com.bs.tool_package;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by 13273 on 2017/10/1.
 *
 */

public class TimeTools {

    public static String generateNumberByTime()
    {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return dateFormat.format(now.getTime());
    }

    public static String generateCurrentTime()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        return dateFormat.format(new Date().getTime());
    }

    public static String generateContentFormatTime()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        return dateFormat.format(new Date().getTime());
    }

    public static String generateDetailTime()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日  HH:mm");
        return dateFormat.format(new Date().getTime());
    }

    public static String generateCustomTime(String format){
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(new Date().getTime());
    }

    public static String parseDetailTime(String time){
        String string = "";
        String[] infoPiece = time.split("-");
        int flag = 0;
        //今年
        if(infoPiece[0].equals(TimeTools.generateCustomTime("yyyy"))) flag +=1;
        //月份日期和今天一样
        if(infoPiece[1].equals(TimeTools.generateCustomTime("MM")) && infoPiece[2].equals(TimeTools.generateCustomTime("dd"))) flag +=2;

        switch (flag){

            case 2:

            case 0: string = infoPiece[0]+"年"+infoPiece[1]+"月"+infoPiece[2]+"日  "+infoPiece[3]+":"+infoPiece[4];
                break;

            case 1: string = infoPiece[1]+"月"+infoPiece[2]+"日  "+infoPiece[3]+":"+infoPiece[4];
                break;

            case 3: string = "今天  "+infoPiece[3]+":"+infoPiece[4];
                break;
        }

        return string;
    }

    public static String parseMessageTime(String time){
        String string = "";
        String[] infoPiece = time.split("-");

        Calendar beforeDay = Calendar.getInstance();
        beforeDay.set(Calendar.DATE, beforeDay.get(Calendar.DATE) - 1);//获取今天的前一天
        String chatStr = infoPiece[0]+"-"+infoPiece[1]+"-"+infoPiece[2];
        String beforeStr = beforeDay.get(Calendar.YEAR)+"-"+(beforeDay.get(Calendar.MONTH)+1)+"-"+beforeDay.get(Calendar.DATE);
        if(chatStr.equals(beforeStr)) return "昨天  "+infoPiece[3]+":"+infoPiece[4];

        int flag = 0;
        //今年
        if(infoPiece[0].equals(TimeTools.generateCustomTime("yyyy"))) flag +=1;
        //月份日期和今天一样
        if(infoPiece[1].equals(TimeTools.generateCustomTime("MM")) && infoPiece[2].equals(TimeTools.generateCustomTime("dd"))) flag +=2;

        switch (flag){

            case 2:

            case 0:
                string = infoPiece[0]+"/"+infoPiece[1]+"/"+infoPiece[2]+"  "+infoPiece[3]+":"+infoPiece[4];
                break;

            //今年
            case 1:
                string = infoPiece[1]+"/"+infoPiece[2]+"  "+infoPiece[3]+":"+infoPiece[4];
                break;

            case 3:
                string = infoPiece[3]+":"+infoPiece[4];
                break;
        }

        return string;
    }

    /**
     * 与上面区别是时间显示格式：年-月-日而不是/-/-/
     * @param time
     * @return
     */
    public static String parseChatTime(String time){
        String string = "";
        String[] infoPiece = time.split("-");

        Calendar beforeDay = Calendar.getInstance();
        beforeDay.set(Calendar.DATE, beforeDay.get(Calendar.DATE) - 1);//获取今天的前一天
        String chatStr = infoPiece[0]+"-"+infoPiece[1]+"-"+infoPiece[2];
        String beforeStr = beforeDay.get(Calendar.YEAR)+"-"+(beforeDay.get(Calendar.MONTH)+1)+"-"+beforeDay.get(Calendar.DATE);
        if(chatStr.equals(beforeStr)) return "昨天  "+infoPiece[3]+":"+infoPiece[4];

        int flag = 0;
        //今年
        if(infoPiece[0].equals(TimeTools.generateCustomTime("yyyy"))) flag +=1;
        //月份日期和今天一样
        if(infoPiece[1].equals(TimeTools.generateCustomTime("MM")) && infoPiece[2].equals(TimeTools.generateCustomTime("dd"))) flag +=2;

        switch (flag){

            case 2:

            case 0:
                string = infoPiece[0]+"年"+infoPiece[1]+"月"+infoPiece[2]+"日  "+infoPiece[3]+":"+infoPiece[4];
                break;

            //今年
            case 1:
                string = infoPiece[1]+"月"+infoPiece[2]+"日  "+infoPiece[3]+":"+infoPiece[4];
                break;

            case 3:
                string = infoPiece[3]+":"+infoPiece[4];
                break;
        }

        return string;
    }
}
