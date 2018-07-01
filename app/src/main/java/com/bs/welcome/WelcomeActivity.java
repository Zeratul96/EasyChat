package com.bs.welcome;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.bs.database.DataBaseUtil;
import com.bs.easy_chat.R;
import com.bs.main.MainActivity;
import com.bs.parameter.Constant;
import com.bs.parameter.SQLLiteConstant;
import com.bs.tool_package.FastJSON;
import com.bs.util.LocalDataIOUtil;
import com.bs.util.NetConnectionUtil;

import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class WelcomeActivity extends Activity {

    ImageView welcomeImg;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //设置为全屏模式
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //同步地区信息
        queryAreaInfo();

        //切换到布局
        setContentView(R.layout.welcome_layout);
        welcomeImg = this.findViewById(R.id.welcome_img);

        //开启动画
        AlphaAnimation an = new AlphaAnimation(0.3f, 1.0f);
        an.setDuration(800);// 设置动画显示时间
        welcomeImg.startAnimation(an);
        an.setAnimationListener(new AnimationImpl());
    }

    private void queryAreaInfo(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap<String ,String> map = new HashMap<>();
                map.put("msgType", Constant.QUERY_AREA);
                String result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(), 0);
                if(!result.equals(Constant.SERVER_CONNECTION_ERROR)&&!result.equals("[null]")){
                    List<Map<String,String>> localData = DataBaseUtil.queryArea();
                    LocalDataIOUtil.syncLocalData(SQLLiteConstant.AREA_TABLE, localData, FastJSON.parseJSON2ListString(result), null, false);
                }
            }
        }).start();
    }


    private class AnimationImpl implements Animation.AnimationListener
    {
        @Override
        public void onAnimationStart(Animation animation) {
            welcomeImg.setBackgroundResource(R.drawable.welcome);
        }

        @Override
        public void onAnimationEnd(Animation animation)
        {
            final SharedPreferences sp = getSharedPreferences("loginInfo", MODE_PRIVATE);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("autoLogin" ,true);
                    startActivity(new Intent(WelcomeActivity.this, sp.getBoolean("autoLogin", false)? MainActivity.class:LoginActivity.class).putExtras(bundle));
                    finish();
                }
            },2000);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {}

    }
}




