package com.bs.person.settings;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

/**
 * Created by 13273 on 2017/9/19.
 *
 */

public class FeedbackActivity extends BaseActivity implements View.OnClickListener,View.OnTouchListener,TextWatcher{

    LinearLayout[] selections;
    View[] lines;

    Button commitBtn;
    EditText editText;

    private int selection = -1;
    final int DEVIATION = 10;
    float originY;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feedback_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");
        if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editText = (EditText) findViewById(R.id.detail_feedback);
        editText.addTextChangedListener(this);

        commitBtn = (Button) findViewById(R.id.commit_btn);
        commitBtn.setOnClickListener(this);

        selections = new LinearLayout[]
                {
                    (LinearLayout) findViewById(R.id.error),(LinearLayout) findViewById(R.id.stack),
                    (LinearLayout) findViewById(R.id.down), (LinearLayout) findViewById(R.id.UI_problem)
                };

        lines = new View[]
                {
                    findViewById(R.id.line0),findViewById(R.id.line1),findViewById(R.id.line2),findViewById(R.id.line3),
                    findViewById(R.id.line4)
                };

        for(int i = 0; i < 4; i++)
        {
            selections[i].setOnClickListener(this);
            selections[i].setOnTouchListener(this);
        }
    }

    @Override
    public void onClick(View view) {

        if(selection != -1)
        ((ImageView)selections[selection].getChildAt(1)).setImageDrawable(null);

        if(view != commitBtn)
        {
            commitBtn.setEnabled(true);

            if(view == selections[0])
                selection = 0;

            else if(view == selections[1])
                selection = 1;

            else if(view == selections[2])
                selection = 2;

            else if(view == selections[3])
                selection = 3;

            ((ImageView)selections[selection].getChildAt(1)).setImageDrawable(ContextCompat.getDrawable(this,R.drawable.blue_circle));

        }

        else
        {
            final Map<String,String > map = new HashMap<>();
            map.put("msgType", Constant.INSERT_FEEDBACK);
            map.put("userID", Preference.userInfoMap.get("user_id"));
            map.put("title", selection != -1?((TextView)selections[selection].getChildAt(0)).getText().toString():"");
            map.put("content", editText.getText().toString());
            map.put("contact", Preference.userInfoMap.get("tel"));

            final Handler myHandler = new Handler()
            {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);

                    switch (msg.what)
                    {
                        case HandlerConstant.SERVER_CONNECTION_ERROR:
                            Toast.makeText(FeedbackActivity.this, "网络连接异常，请检查网络连接。", Toast.LENGTH_SHORT).show();
                            break;

                        case HandlerConstant.OPERATION_SUCCEED:
                            editText.clearFocus();
                            Toast.makeText(FeedbackActivity.this, "提交成功，感谢您的反馈。", Toast.LENGTH_SHORT).show();
                            commitBtn.setOnClickListener(null);
                            for(int i=0;i<4;i++)
                                selections[i].setOnClickListener(null);

                            postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                }
                            },300);
                            break;
                    }
                }
            };

            new Thread(new Runnable()
            {
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

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        final int TOP_Y = 0;
        final int BOTTOM_Y = view.getBottom() - view.getTop();

        switch (motionEvent.getAction())
        {
            case MotionEvent.ACTION_DOWN:

                originY = motionEvent.getY();

                view.setBackgroundColor(ContextCompat.getColor(this, R.color.pressed_background));

                if(view == selections[0])
                    lines[1].setBackgroundColor(ContextCompat.getColor(this, R.color.white));

                else if(view == selections[1])
                {
                    lines[1].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                    lines[2].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                }

                else if(view == selections[2])
                {
                    lines[2].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                    lines[3].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                }

                else if(view == selections[3])
                    lines[3].setBackgroundColor(ContextCompat.getColor(this, R.color.white));

                break;

            case MotionEvent.ACTION_UP:
                    resetLinesColor();
                    view.setBackgroundColor(Color.parseColor("#FFFFFF"));

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
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        if(editText.getText().toString().length()>0)
            commitBtn.setEnabled(true);

        else if(selection == -1)
            commitBtn.setEnabled(false);

    }

    @Override
    public void afterTextChanged(Editable editable) {}

    private void resetLinesColor()
    {
        lines[1].setBackgroundColor(ContextCompat.getColor(this, R.color.line_gray));
        lines[2].setBackgroundColor(ContextCompat.getColor(this, R.color.line_gray));
        lines[3].setBackgroundColor(ContextCompat.getColor(this, R.color.line_gray));
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
}
