package com.bs.person.settings;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.bs.easy_chat.R;
import com.bs.util.BaseActivity;

public class AboutActivity extends BaseActivity implements View.OnClickListener,View.OnTouchListener{

    LinearLayout[] selections;
    View[] lines;
    final int DEVIATION = 10;
    float originY;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");
        if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lines = new View[]{findViewById(R.id.line0),findViewById(R.id.line1),findViewById(R.id.line2)};
        selections = new LinearLayout[]{(LinearLayout)findViewById(R.id.feedback_layout),(LinearLayout) findViewById(R.id.author_layout)};

        for(int i = 0; i < 2; i++) {
            selections[i].setOnClickListener(this);
            selections[i].setOnTouchListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        for(int i = 0; i < 2; i++)
            selections[i].setBackgroundColor(Color.parseColor("#FFFFFF"));
        resetLinesColor();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent)
    {
        final int TOP_Y = 0;
        final int BOTTOM_Y = view.getBottom() - view.getTop();

        switch (motionEvent.getAction())
        {
            case MotionEvent.ACTION_DOWN:

                originY = motionEvent.getY();

                view.setBackgroundColor(Color.parseColor("#d9d9d9"));

                //lines color change
                if(view == selections[0] || view == selections[1])
                    lines[1].setBackgroundColor(Color.parseColor("#FFFFFF"));
                break;

            case MotionEvent.ACTION_UP:

            case MotionEvent.ACTION_MOVE:
                if(motionEvent.getY() < TOP_Y|| motionEvent.getY() > BOTTOM_Y || Math.abs(originY-motionEvent.getY()) > DEVIATION)
                {
                    resetLinesColor();
                    view.setBackgroundColor(Color.parseColor("#FFFFFF"));

                    return true;
                }

                break;

        }
        return false;
    }

    @Override
    public void onClick(View view)
    {
        if(view == selections[0])
            startActivity(new Intent(this, FeedbackActivity.class));

        else if(view == selections[1])
            startActivity(new Intent(this, AuthorActivity.class));
    }

    private void resetLinesColor()
    {
        lines[1].setBackgroundColor(Color.parseColor("#d9d9d9"));
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
