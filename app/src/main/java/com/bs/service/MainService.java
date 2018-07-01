package com.bs.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.bs.util.NetConnectionUtil;

public class MainService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        NetConnectionUtil.cat.setContext(this);
        NetConnectionUtil.cat.start();//只由后台控制通信线程启动
    }

    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}
