package com.bs.person.settings;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bs.easy_chat.R;
import com.bs.parameter.Constant;
import com.bs.parameter.Preference;
import com.bs.util.BaseActivity;
import com.bs.util.MainHandler;
import com.bs.util.NetConnectionUtil;
import com.bs.widget.ClearEditText;

import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PasswordActivity extends BaseActivity implements View.OnClickListener,TextWatcher{

    TextView finishBtn;
    TextView cancelBtn;

    ClearEditText original;
    ClearEditText password;
    ClearEditText confirm;

    AlertDialog alertDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password_layout);

        original = (ClearEditText) findViewById(R.id.origin_edit);
        password = (ClearEditText) findViewById(R.id.password_edit);
        confirm = (ClearEditText) findViewById(R.id.confirm_edit);

        original.addTextChangedListener(this);
        password.addTextChangedListener(this);
        confirm.addTextChangedListener(this);

        finishBtn = (TextView) findViewById(R.id.finish_btn);
        finishBtn.setOnClickListener(this);

        cancelBtn = (TextView) findViewById(R.id.cancel_btn);
        cancelBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        if(view == cancelBtn) finish();

        else
        {
            if(!original.getText().toString().equals(Preference.userInfoMap.get("password")))
            {
                showAlertDialog("原密码输入错误，请核对后重试。");
                return;
            }

            if(!password.getText().toString().equals(confirm.getText().toString()))
            {
                showAlertDialog("两次输入的密码不一致，请核对后重试。");
                return;
            }

            final Map<String , String > map = new HashMap<>();
            map.put("msgType", Constant.UPDATE_USER);
            map.put("mode", "10");
            map.put("userID", Preference.userInfoMap.get("user_id"));
            map.put("password", password.getText().toString());

            new Thread(new Runnable() {
                @Override
                public void run() {

                    String result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(),0);

                    switch (result)
                    {
                        case "[null]":

                        case Constant.SERVER_CONNECTION_ERROR:
                            MainHandler.getInstance().post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(PasswordActivity.this, "网络连接异常，请检查网络连接。", Toast.LENGTH_SHORT).show();
                                }
                            });
                            break;

                        default:
                            MainHandler.getInstance().post(new Runnable() {
                                @Override
                                public void run() {
                                    Preference.userInfoMap.put("password",password.getText().toString());
                                    SharedPreferences.Editor editor = getSharedPreferences("userInfo", MODE_PRIVATE).edit();
                                    editor.putString("userInfo", JSONObject.fromObject(Preference.userInfoMap).toString());
                                    editor.apply();
                                    finish();
                                }
                            });
                    }
                }
            }).start();


        }

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if(original.getText().toString().length()==0||password.getText().toString().length()==0||confirm.getText().toString().length()==0)
            finishBtn.setEnabled(false);

        else finishBtn.setEnabled(true);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    @Override
    public void afterTextChanged(Editable editable) {}

    private void showAlertDialog(String msg)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.AlertDialogCustom);builder.setTitle("提示");
        //对话框内容
        builder.setMessage(msg);
        //确定按钮
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
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
