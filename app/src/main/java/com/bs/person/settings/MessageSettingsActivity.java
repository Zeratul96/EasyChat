package com.bs.person.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.bs.easy_chat.R;
import com.bs.parameter.Preference;
import com.bs.util.BaseActivity;
import com.bs.widget.MyToggleButton;

public class MessageSettingsActivity extends BaseActivity implements View.OnClickListener{

    MyToggleButton notificationBtn;
    MyToggleButton detailBtn;

    SharedPreferences sp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_settings_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");
        if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        notificationBtn = (MyToggleButton) findViewById(R.id.notification_btn);
        detailBtn = (MyToggleButton) findViewById(R.id.detail_btn);

        notificationBtn.setOnToggleStateListener(new MyToggleButton.OnToggleListener() {
            @Override
            public void onToggleSate(boolean state) {
                Preference.isNotified = state;
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("notification", state);
                editor.apply();
            }
        });

        detailBtn.setOnToggleStateListener(new MyToggleButton.OnToggleListener() {
            @Override
            public void onToggleSate(boolean state) {
                Preference.isShownDetail = state;
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("detail", state);
                editor.apply();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        sp = getSharedPreferences("settingsInfo", MODE_PRIVATE);
        notificationBtn.setToggleState(Preference.isNotified);
        detailBtn.setToggleState(Preference.isShownDetail);
    }

    @Override
    public void onClick(View v) {
        finish();
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
