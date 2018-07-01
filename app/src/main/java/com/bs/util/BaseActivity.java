package com.bs.util;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by 13273 on 2018/1/1.
 * Activity基本类
 */

public class BaseActivity extends AppCompatActivity{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityListUtil.addActivityToList(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityListUtil.removeActivityFromList(this);
    }
}
