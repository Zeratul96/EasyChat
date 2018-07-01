package com.bs.welcome;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bs.easy_chat.R;
import com.bs.main.MainActivity;
import com.bs.parameter.Constant;
import com.bs.parameter.Preference;
import com.bs.tool_package.FastJSON;
import com.bs.util.NetConnectionUtil;

import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends Activity implements TextWatcher,View.OnClickListener{

    private Button loginBtn;
    private EditText userIDEdit;
    private EditText passwordEdit;
    private SharedPreferences sp;

    private final int NETWORK_FAILURE = -1;
    private final int ID_NOT_EXIST = 0;
    private final int PASSWORD_INCORRECT = 1;
    private final int ID_BANNED = 2;
    private final int LOGIN = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        loginBtn = findViewById(R.id.login_button);
        userIDEdit = findViewById(R.id.user_id);
        passwordEdit = findViewById(R.id.password);

        TextView registerBtn = findViewById(R.id.register_btn);
        registerBtn.setOnClickListener(this);

        userIDEdit.addTextChangedListener(this);
        passwordEdit.addTextChangedListener(this);

        loginBtn.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        sp = getSharedPreferences("loginInfo", MODE_PRIVATE);

        String userIDStr = sp.getString("userID", null);
        String passwordStr = sp.getString("password", null);

        if(userIDStr != null){
            userIDEdit.setText(userIDStr);
            userIDEdit.requestFocus();
            userIDEdit.setSelection(userIDStr.length());
        }

        if(passwordStr != null){
            passwordEdit.setText(passwordStr);
            passwordEdit.requestFocus();
            passwordEdit.setSelection(passwordStr.length());
        }

    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    @Override
    public void afterTextChanged(Editable editable) {}

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if(userIDEdit.getText().toString().length()>0&&passwordEdit.getText().toString().length()>0)
            loginBtn.setEnabled(true);

        else
            loginBtn.setEnabled(false);

    }


    @Override
    public void onClick(View view) {

        if(view == loginBtn)
        {
            final Handler myHandler = new Handler()
            {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);

                    switch (msg.what)
                    {
                        case NETWORK_FAILURE:
                            Toast.makeText(LoginActivity.this, "网络连接异常，请检查网络连接。", Toast.LENGTH_SHORT).show();
                            break;

                        case ID_NOT_EXIST:
                            Toast.makeText(LoginActivity.this, "账号不存在，请检查账号后再次尝试。", Toast.LENGTH_SHORT).show();
                            break;

                        case PASSWORD_INCORRECT:
                            Toast.makeText(LoginActivity.this, "密码输入错误，请检查密码后再次尝试。", Toast.LENGTH_SHORT).show();
                            break;

                        case ID_BANNED:
                            Toast.makeText(LoginActivity.this, "该账号已被禁封，禁止登录。", Toast.LENGTH_SHORT).show();
                            break;

                        case LOGIN:
                            Bundle bundle = new Bundle();
                            bundle.putBoolean("autoLogin", false);
                            startActivity(new Intent(LoginActivity.this, MainActivity.class).putExtras(bundle));
                            finish();
                            break;
                    }
                }
            };

            final Map<String, String> map = new HashMap<>();
            map.put("msgType", Constant.LOGIN_CHECK);
            map.put("userID", userIDEdit.getText().toString());

            new Thread(new Runnable() {
                @Override
                public void run() {

                    String result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(),0);

                    Message msg = new Message();
                    if(result.equals(Constant.SERVER_CONNECTION_ERROR))
                        msg.what = NETWORK_FAILURE;

                    else if(result.equals("[null]"))
                        msg.what = ID_NOT_EXIST;

                    else{
                        Preference.userInfoMap = FastJSON.parseJSON2ListString(result).get(0);

                        if(Preference.userInfoMap.get("password").equals(passwordEdit.getText().toString())){
                            if(Preference.userInfoMap.get("banning").equals("0"))
                                msg.what = ID_BANNED;

                            else{
                                msg.what = LOGIN;
                                saveLoginInfo();
                            }
                        }
                        else
                            msg.what = PASSWORD_INCORRECT;
                    }

                    myHandler.sendMessage(msg);

                }
            }).start();
        }

        else
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));

    }


    private void saveLoginInfo()
    {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("userID", Preference.userInfoMap.get("user_id"));
        editor.putString("password", Preference.userInfoMap.get("password"));
        editor.putBoolean("autoLogin", true);
        editor.apply();

        editor = getSharedPreferences("userInfo", MODE_PRIVATE).edit();
        editor.putString("userInfo", JSONObject.fromObject(Preference.userInfoMap).toString());
        editor.apply();
    }
}
