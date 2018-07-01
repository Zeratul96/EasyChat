package com.bs.person.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.bs.easy_chat.R;
import com.bs.parameter.Preference;
import com.bs.util.BaseActivity;
import com.bs.widget.MyToggleButton;

public class MomentSettingsActivity extends BaseActivity{

    SharedPreferences sp;

    MyToggleButton syncBtn;
    MyToggleButton draftBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.moment_settings_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");
        if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        syncBtn = (MyToggleButton) findViewById(R.id.sync_btn);
        draftBtn = (MyToggleButton) findViewById(R.id.draft_btn);
        syncBtn.setOnToggleStateListener(new MyToggleButton.OnToggleListener() {
            @Override
            public void onToggleSate(boolean state) {
                Preference.isSync = state;
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("sync",state);
                editor.apply();
            }
        });

        draftBtn.setOnToggleStateListener(new MyToggleButton.OnToggleListener() {
            @Override
            public void onToggleSate(boolean state) {
                Preference.isDraft = state;
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("draft",state);
                editor.apply();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        sp = getSharedPreferences("settingsInfo", MODE_PRIVATE);
        syncBtn.setToggleState(Preference.isSync);
        draftBtn.setToggleState(Preference.isDraft);
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
