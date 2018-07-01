package com.bs.person.person_center;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
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

public class NameActivity extends BaseActivity implements View.OnClickListener,TextWatcher{

    private TextView cancelBtn;
    private TextView finishBtn;
    private EditText editText;
    private String infoStr;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.name_layout);

        cancelBtn = (TextView) findViewById(R.id.cancel_btn);
        finishBtn = (TextView) findViewById(R.id.finish_btn);
        editText = (EditText) findViewById(R.id.editText);
        editText.addTextChangedListener(this);

        infoStr = Preference.userInfoMap.get("name");
        if(infoStr != null)
        {
            editText.setText(infoStr);
            editText.setSelection(infoStr.length());
        }

        cancelBtn.setOnClickListener(this);
        finishBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        if(view == cancelBtn)
            finish();

        else{
            final Map<String , String > map = new HashMap<>();
            map.put("msgType", Constant.UPDATE_USER);
            map.put("mode", "1");
            map.put("userID", Preference.userInfoMap.get("user_id"));
            map.put("name", editText.getText().toString());

            final Handler myHandler = new Handler()
            {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);

                    switch (msg.what)
                    {
                        case HandlerConstant.SERVER_CONNECTION_ERROR:
                            Toast.makeText(NameActivity.this, "网络连接异常，请检查网络连接。", Toast.LENGTH_SHORT).show();
                            break;

                        case HandlerConstant.OPERATION_SUCCEED:
                            Preference.userInfoMap.put("name",editText.getText().toString());
                            SharedPreferences.Editor editor = getSharedPreferences("userInfo", MODE_PRIVATE).edit();
                            editor.putString("userInfo",JSONObject.fromObject(Preference.userInfoMap).toString());
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

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if(editText.getText().toString().length()>0 && !editText.getText().toString().equals(infoStr))
            finishBtn.setEnabled(true);

        else
            finishBtn.setEnabled(false);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    @Override
    public void afterTextChanged(Editable editable) {}
}
