package com.bs.person.person_center;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bs.easy_chat.R;
import com.bs.parameter.Constant;
import com.bs.parameter.Preference;
import com.bs.util.BaseActivity;
import com.bs.util.MainHandler;
import com.bs.util.NetConnectionUtil;

import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TelActivity extends BaseActivity implements TextWatcher,View.OnClickListener{

    private TextView cancelBtn;
    private TextView finishBtn;
    private EditText editText;
    private String infoStr;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tel_layout);

        cancelBtn = (TextView) findViewById(R.id.cancel_btn);
        finishBtn = (TextView) findViewById(R.id.finish_btn);
        editText = (EditText) findViewById(R.id.editText);
        editText.addTextChangedListener(this);

        infoStr = Preference.userInfoMap.get("tel");
        if(infoStr != null){
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
            map.put("mode", "5");
            map.put("userID", Preference.userInfoMap.get("user_id"));
            map.put("tel", editText.getText().toString());

            new Thread(new Runnable() {
                @Override
                public void run() {

                    final String result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(),0);

                    MainHandler.getInstance().post(new Runnable() {
                        @Override
                        public void run() {

                            switch (result)
                            {
                                case "[null]":

                                case Constant.SERVER_CONNECTION_ERROR:
                                    Toast.makeText(TelActivity.this, "网络连接异常，请检查网络连接。", Toast.LENGTH_SHORT).show();
                                    break;

                                default:
                                    Preference.userInfoMap.put("tel",editText.getText().toString());
                                    SharedPreferences.Editor editor = getSharedPreferences("userInfo", MODE_PRIVATE).edit();
                                    editor.putString("userInfo",JSONObject.fromObject(Preference.userInfoMap).toString());
                                    editor.apply();
                                    finish();
                                    break;
                            }
                        }
                    });
                }
            }).start();
        }
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if(editText.getText().toString().length()>0 && !editText.getText().toString().equals(infoStr)&&
           editText.getText().toString().length()==11)

            finishBtn.setEnabled(true);

        else
            finishBtn.setEnabled(false);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    @Override
    public void afterTextChanged(Editable editable) {}
}
