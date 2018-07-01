package com.bs.util;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by 13273 on 2017/10/17.
 *
 */

public class MainHandler extends Handler{
    private static volatile MainHandler instance;

    public static MainHandler getInstance(){
        if(null == instance){
            synchronized (MainHandler.class){
                if(null == instance)
                    instance = new MainHandler();
            }
        }

        return instance;
    }

    private MainHandler(){
        super(Looper.getMainLooper());
    }
}
