package com.bs.person.person_center;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bs.easy_chat.R;
import com.bs.parameter.Constant;
import com.bs.parameter.HandlerConstant;
import com.bs.parameter.Preference;
import com.bs.util.BaseActivity;
import com.bs.util.NetConnectionUtil;

import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class GenderActivity extends BaseActivity implements View.OnClickListener,View.OnTouchListener{

    LinearLayout[] selections;

    private int selection = -1;

    View lines;

    TextView cancelBtn;

    TextView finishBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gender_layout);

        cancelBtn = (TextView) findViewById(R.id.cancel_btn);
        cancelBtn.setOnClickListener(this);

        finishBtn = (TextView) findViewById(R.id.finish_btn);
        finishBtn.setOnClickListener(this);

        selections = new LinearLayout[]{(LinearLayout) findViewById(R.id.male_layout),(LinearLayout) findViewById(R.id.female_layout)};

        for(int i=0;i<2;i++){
            selections[i].setOnClickListener(this);
            selections[i].setOnTouchListener(this);
        }

        lines = findViewById(R.id.line1);
    }

    @Override
    protected void onStart() {
        super.onStart();

        String gender = Preference.userInfoMap.get("gender");

        if(!gender.equals(""))
        {
            selection = (gender.equals("男"))? 0 : 1;
            ((ImageView)selections[selection].getChildAt(1)).setImageDrawable(ContextCompat.getDrawable(this,R.drawable.blue_circle));
        }
    }

    @Override
    public void onClick(View view)
    {
        if(view == cancelBtn)
            finish();

        else if(view == selections[0])
        {
            finishBtn.setEnabled(true);
            selection = 0;
            ((ImageView)selections[selection].getChildAt(1)).setImageDrawable(ContextCompat.getDrawable(this,R.drawable.blue_circle));
            ((ImageView)selections[1].getChildAt(1)).setImageDrawable(null);
        }

        else if(view == selections[1])
        {
            finishBtn.setEnabled(true);
            selection = 1;
            ((ImageView)selections[selection].getChildAt(1)).setImageDrawable(ContextCompat.getDrawable(this,R.drawable.blue_circle));
            ((ImageView)selections[0].getChildAt(1)).setImageDrawable(null);
        }

        else if(view == finishBtn)
            uploadInfo((selection == 0)?"男":"女");

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        final int TOP_Y = 0;
        final int BOTTOM_Y = view.getBottom() - view.getTop();
        final int DEVIATION = 15;

        switch (motionEvent.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                lines.setBackgroundColor(Color.parseColor("#FFFFFF"));
                view.setBackgroundColor(ContextCompat.getColor(GenderActivity.this, R.color.pressed_background));
                break;

            case MotionEvent.ACTION_UP:
                lines.setBackgroundColor(ContextCompat.getColor(GenderActivity.this,R.color.line_gray));
                view.setBackgroundColor(Color.parseColor("#FFFFFF"));
                break;

            case MotionEvent.ACTION_MOVE:
                if(motionEvent.getY() < TOP_Y - DEVIATION|| motionEvent.getY() > BOTTOM_Y + DEVIATION)
                {
                    lines.setBackgroundColor(ContextCompat.getColor(GenderActivity.this,R.color.line_gray));
                    view.setBackgroundColor(Color.parseColor("#FFFFFF"));
                }
                break;

        }


        return false;
    }

    private void uploadInfo(final String gender)
    {
        final Map<String , String > map = new HashMap<>();
        map.put("msgType", Constant.UPDATE_USER);
        map.put("mode", "4");
        map.put("userID", Preference.userInfoMap.get("user_id"));
        map.put("gender", gender);

        final Handler myHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                switch (msg.what)
                {
                    case HandlerConstant.SERVER_CONNECTION_ERROR:
                        Toast.makeText(GenderActivity.this, "网络连接异常，请检查网络连接。", Toast.LENGTH_SHORT).show();
                        break;

                    case HandlerConstant.OPERATION_SUCCEED:
                        //信息存储至本地与内存中
                        Preference.userInfoMap.put("gender",gender);
                        SharedPreferences.Editor editor = getSharedPreferences("userInfo", MODE_PRIVATE).edit();
                        editor.putString("userInfo", JSONObject.fromObject(Preference.userInfoMap).toString());
                        editor.apply();
                        finish();
                        break;
                }
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {

                Message message = new Message();

                String result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(),0);

                switch (result)
                {
                    case "[null]":

                    case Constant.SERVER_CONNECTION_ERROR:
                        message.what = HandlerConstant.SERVER_CONNECTION_ERROR;
                        break;

                    default:
                        message.what = HandlerConstant.OPERATION_SUCCEED;
                        break;
                }

                myHandler.sendMessage(message);
            }
        }).start();

    }
}
