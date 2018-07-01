package com.bs.welcome;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bs.easy_chat.R;
import com.bs.main.MainActivity;
import com.bs.parameter.Constant;
import com.bs.parameter.Preference;
import com.bs.tool_package.FastJSON;
import com.bs.util.MainHandler;
import com.bs.util.NetConnectionUtil;

import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener,TextWatcher{

    Button registerBtn;
    EditText IDEdit;
    EditText passwordEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");
        if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        IDEdit = (EditText) findViewById(R.id.account_edit);
        passwordEdit = (EditText) findViewById(R.id.password_edit);

        IDEdit.addTextChangedListener(this);
        passwordEdit.addTextChangedListener(this);

        registerBtn = (Button) findViewById(R.id.register_btn);
        registerBtn.setOnClickListener(this);
    }


    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    @Override
    public void afterTextChanged(Editable editable) {}

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            registerBtn.setEnabled(IDEdit.getText().toString().length()>0 && passwordEdit.getText().toString().length()>0);
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
    public void onClick(View view) {

        final Map<String , String > map = new HashMap<>();
        map.put("msgType", Constant.INSERT_USER);
        map.put("userID",IDEdit.getText().toString());
        map.put("password", passwordEdit.getText().toString());

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
                                Toast.makeText(RegisterActivity.this, getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                                break;

                            default:
                                List<Map<String ,String>> list = FastJSON.parseJSON2ListString(result);
                                Map<String , String > resultMap = list.get(0);

                                if(resultMap.get("result").equals(Constant.OPERATION_SUCCEED)){
                                    saveLoginInfo();
                                    Bundle bundle = new Bundle();
                                    bundle.putBoolean("autoLogin", false);
                                    startActivity(new Intent(RegisterActivity.this, MainActivity.class).putExtras(bundle));
                                    finish();
                                }
                                else
                                    Toast.makeText(RegisterActivity.this, "抱歉，易聊号已存在，请更换一个再次尝试。", Toast.LENGTH_SHORT).show();

                                break;
                        }
                    }
                });
            }
        }).start();
    }

    private void saveLoginInfo(){
        SharedPreferences sp = getSharedPreferences("loginInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("userID", IDEdit.getText().toString());
        editor.putString("password", passwordEdit.getText().toString());
        editor.putBoolean("autoLogin", true);
        editor.apply();

        //直接存储信息
        Preference.userInfoMap = new HashMap<>();
        Preference.userInfoMap.put("user_id", IDEdit.getText().toString());
        Preference.userInfoMap.put("areas", "-1");
        Preference.userInfoMap.put("password", passwordEdit.getText().toString());
        Preference.userInfoMap.put("nickname", IDEdit.getText().toString());
        Preference.userInfoMap.put("name", "");
        Preference.userInfoMap.put("gender", "");
        Preference.userInfoMap.put("tel", "");
        Preference.userInfoMap.put("email","");
        Preference.userInfoMap.put("handwriting","");
        Preference.userInfoMap.put("self_introduction","");
        Preference.userInfoMap.put("sculpture","");
        Preference.userInfoMap.put("moments_background","");
        Preference.userInfoMap.put("banning","1");

        editor = getSharedPreferences("userInfo", MODE_PRIVATE).edit();
        editor.putString("userInfo", JSONObject.fromObject(Preference.userInfoMap).toString());
        editor.apply();
    }


}
