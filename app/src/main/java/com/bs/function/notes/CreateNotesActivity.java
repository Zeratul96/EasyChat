package com.bs.function.notes;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bs.database.DataBaseUtil;
import com.bs.easy_chat.R;
import com.bs.parameter.Preference;
import com.bs.parameter.SQLLiteConstant;
import com.bs.tool_package.TimeTools;
import com.bs.util.BaseActivity;

import java.util.HashMap;
import java.util.Map;

public class CreateNotesActivity extends BaseActivity implements View.OnClickListener{

    LinearLayout backLayout;
    TextView finishBtn;
    EditText editText;
    TextView detailDate;
    Map<String,String> info;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_notes_layout);

        backLayout = (LinearLayout) findViewById(R.id.back_layout);
        backLayout.setOnClickListener(this);

        finishBtn = (TextView) findViewById(R.id.finish_btn);
        finishBtn.setOnClickListener(this);

        editText = (EditText) findViewById(R.id.editText);
        detailDate = (TextView) findViewById(R.id.detail_date);
    }

    @Override
    protected void onStart() {
        super.onStart();

        info = (HashMap<String,String>)getIntent().getSerializableExtra("msg");
        if(info != null){
            editText.setText(info.get("content"));
            editText.setSelection(editText.getText().toString().length());

            detailDate.setText((info.get("date").equals(TimeTools.generateContentFormatTime()))?
                    "今天  "+info.get("detail_date").substring(13, 18):info.get("detail_date"));
        }
        else
            detailDate.setText("今天  "+TimeTools.generateCurrentTime());
    }

    @Override
    public void onClick(View v) {
        if(v == backLayout)
            finish();

        else
        {
            //编辑已有的备忘录
            if(info != null){
                if(editText.getText().toString().length() > 0)
                {
                    String sql = "update notes set date = '"
                            +TimeTools.generateContentFormatTime()
                            +"',detail_date = '"
                            +TimeTools.generateDetailTime()
                            +"',content = '"+editText.getText().toString()
                            +"',primary_key = '"+TimeTools.generateNumberByTime()
                            +"' where primary_key = '"+info.get("primary_key")+"'";
                    DataBaseUtil.update(sql, SQLLiteConstant.NOTES_TABLE);
                }

                else
                {
                    String sql = "delete from notes where primary_key = '"+info.get("primary_key")+"'";
                    DataBaseUtil.delete(sql,SQLLiteConstant.NOTES_TABLE);
                }
            }
            //创建新的备忘录
            else if(editText.getText().toString().length() > 0)
            {
                String sql = "insert into notes values("
                        + "'"+TimeTools.generateNumberByTime()+"'"
                        + ",'"+TimeTools.generateContentFormatTime()+"'"
                        + ",'"+TimeTools.generateDetailTime()+"'"
                        + ",'"+editText.getText().toString()+"'"
                        + ",'"+ Preference.userInfoMap.get("user_id")+"')";
                DataBaseUtil.insert(sql,SQLLiteConstant.NOTES_TABLE);
            }

            finish();
        }
    }
}
