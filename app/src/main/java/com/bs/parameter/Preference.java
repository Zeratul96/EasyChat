package com.bs.parameter;

import java.util.Map;

/**
 * Created by 13273 on 2017/9/15.
 *存储在内存中的参数
 */

public class Preference {
    public static final String serverAddress = "192.168.186.1";
    public static final String pictureServer = "192.168.186.1";
    public static final String chatServer = "192.168.186.1";
    public static Map<String, String> userInfoMap;

    /**
     * 五个设置项参数
     */
    public static boolean isNotified = false;
    public static boolean isShownDetail = false;
    public static boolean isDraft = false;
    public static boolean isSync = false;
    public static boolean isTimeLineStyle = false;

    /**
     * UI参数
     */
    public static boolean messageSpinning = true;
}
