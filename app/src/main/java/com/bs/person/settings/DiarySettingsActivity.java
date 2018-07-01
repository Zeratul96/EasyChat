package com.bs.person.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.bs.easy_chat.R;
import com.bs.parameter.Preference;
import com.bs.util.BaseActivity;
import com.bs.widget.MyToggleButton;

public class DiarySettingsActivity extends BaseActivity{

    SharedPreferences sp;

    MyToggleButton showBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diary_settings_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        showBtn = (MyToggleButton) findViewById(R.id.show_btn);
        showBtn.setOnToggleStateListener(new MyToggleButton.OnToggleListener() {
            @Override
            public void onToggleSate(boolean state) {
                Preference.isTimeLineStyle = state;
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("timeline", state);
                editor.apply();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        sp = getSharedPreferences("settingsInfo", MODE_PRIVATE);
        showBtn.setToggleState(Preference.isTimeLineStyle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
