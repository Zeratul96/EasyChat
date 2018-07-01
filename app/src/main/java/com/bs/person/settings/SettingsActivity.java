package com.bs.person.settings;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.bs.database.DataBaseUtil;
import com.bs.easy_chat.R;
import com.bs.parameter.ChatServerConstant;
import com.bs.parameter.Preference;
import com.bs.service.MainService;
import com.bs.util.ActivityListUtil;
import com.bs.util.BaseActivity;
import com.bs.util.NetConnectionUtil;
import com.bs.welcome.LoginActivity;

import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends BaseActivity implements View.OnTouchListener, View.OnClickListener{

    View[] lines;
    LinearLayout[] selections;
    Dialog alertDialog;

    float originY;
    final int DEVIATION = 10;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");
        if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        lines = new View[]
                {
                    findViewById(R.id.line0),findViewById(R.id.line1),findViewById(R.id.line2),
                    findViewById(R.id.line3),findViewById(R.id.line4),findViewById(R.id.line5),
                    findViewById(R.id.line6),findViewById(R.id.line7),findViewById(R.id.line8),
                    findViewById(R.id.line9)
                };

        selections = new LinearLayout[]
                {
                    (LinearLayout) findViewById(R.id.account_layout),(LinearLayout) findViewById(R.id.message_layout),
                    (LinearLayout) findViewById(R.id.moments_layout),(LinearLayout) findViewById(R.id.diary_layout),
                    (LinearLayout) findViewById(R.id.about_layout),(LinearLayout) findViewById(R.id.logout_layout)
                };

        for(int i = 0; i < 6; i++)
        {
            selections[i].setOnTouchListener(this);
            selections[i].setOnClickListener(this);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        for(int i = 0; i < 6; i++)
            selections[i].setBackgroundColor(Color.parseColor("#FFFFFF"));

        resetLinesColor();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent)
    {
        final int TOP_Y = 0;
        final int BOTTOM_Y = view.getBottom() - view.getTop();

        switch (motionEvent.getAction())
        {
            case MotionEvent.ACTION_DOWN:

                originY = motionEvent.getY();

                view.setBackgroundColor(Color.parseColor("#d9d9d9"));

                //lines color change
                if(view == selections[0] || view == selections[1])
                    lines[1].setBackgroundColor(Color.parseColor("#FFFFFF"));

                else if(view == selections[2] || view == selections[3])
                    lines[4].setBackgroundColor(Color.parseColor("#FFFFFF"));

                break;

            case MotionEvent.ACTION_UP:
                if(view == selections[5])
                    selections[5].setBackgroundColor(Color.parseColor("#FFFFFF"));

            case MotionEvent.ACTION_MOVE:
                if(motionEvent.getY() < TOP_Y|| motionEvent.getY() > BOTTOM_Y || Math.abs(originY-motionEvent.getY()) > DEVIATION)
                {
                    resetLinesColor();
                    view.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    return true;
                }
                break;
        }
        return false;
    }

    @Override
    public void onClick(View view)
    {
        if(view == selections[0])
            startActivity(new Intent(this, PasswordActivity.class));

        else if(view == selections[1])
            startActivity(new Intent(this, MessageSettingsActivity.class));

        else if(view == selections[2])
            startActivity(new Intent(this, MomentSettingsActivity.class));

        else if(view == selections[3])
            startActivity(new Intent(this, DiarySettingsActivity.class));

        else if(view == selections[4])
            startActivity(new Intent(this, AboutActivity.class));

        else if(view == selections[5])
            showAlertDialog();
    }

    private void resetLinesColor()
    {
        lines[1].setBackgroundColor(Color.parseColor("#d9d9d9"));
        lines[4].setBackgroundColor(Color.parseColor("#d9d9d9"));
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        builder.setTitle("提示");
        //对话框内容
        builder.setMessage("退出登录会删除所有聊天记录，但将保留日记与备忘录，下次登录您仍可查看");
        //确定按钮
        builder.setPositiveButton("退出登录", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //上传断线消息
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Map<String,String> logOutRequest = new HashMap<>();
                        logOutRequest.put("msgType", ChatServerConstant.USER_LOGOUT);
                        logOutRequest.put("user_id", Preference.userInfoMap.get("user_id"));
                        NetConnectionUtil.uploadChatData(JSONObject.fromObject(logOutRequest).toString());
                        NetConnectionUtil.cat.stopThread();
                        NetConnectionUtil.cat.interrupt();
                        stopService(new Intent(SettingsActivity.this, MainService.class));
                        Preference.messageSpinning = true;

                        //删除所有用户数据
                        SharedPreferences.Editor sp2 = getSharedPreferences("userInfo", MODE_PRIVATE).edit();
                        sp2.putString("userInfo", null);
                        sp2.apply();
                    }
                }).start();

                //改变登录信息
                SharedPreferences.Editor sp1 = getSharedPreferences("loginInfo", MODE_PRIVATE).edit();
                sp1.putBoolean("autoLogin", false);
                sp1.commit();

                //结束所有的Activity并跳转到登录界面
                startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
                ActivityListUtil.destroyAllActivity();
                DataBaseUtil.deleteUserData();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });

        //固定对话框使其不可被取消
        builder.setCancelable(false);
        //创建对话框
        alertDialog = builder.create();
        alertDialog.show();
    }


}
