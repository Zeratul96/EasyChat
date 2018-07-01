package com.bs.person.settings;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.bs.easy_chat.R;
import com.bs.util.BaseActivity;

public class AuthorActivity extends BaseActivity{

    View[] lines;
    LinearLayout[] selections;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.author_layout);

        lines = new View[]
                {
                    findViewById(R.id.line0),findViewById(R.id.line1),findViewById(R.id.line2),
                    findViewById(R.id.line3),findViewById(R.id.line4),findViewById(R.id.line5)
                };

        selections = new LinearLayout[]
                {
                    (LinearLayout) findViewById(R.id.qq_layout),(LinearLayout) findViewById(R.id.email_layout),
                    (LinearLayout) findViewById(R.id.wechat_layout),(LinearLayout) findViewById(R.id.weibo_layout),
                    (LinearLayout) findViewById(R.id.tel_layout)
                };

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");
        if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }


    @Override
    protected void onResume() {
        super.onResume();
        for(int i = 0; i < 5; i++)
            selections[i].setBackgroundColor(Color.parseColor("#FFFFFF"));

        resetLinesColor();
    }

    private void resetLinesColor()
    {
        for(int i=0;i<4;i++)
            lines[i+1].setBackgroundColor(Color.parseColor("#d9d9d9"));
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
