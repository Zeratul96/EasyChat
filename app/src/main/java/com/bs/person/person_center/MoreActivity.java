package com.bs.person.person_center;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bs.easy_chat.R;
import com.bs.parameter.Preference;
import com.bs.tool_package.UserInfoHelper;
import com.bs.util.BaseActivity;

public class MoreActivity extends BaseActivity implements View.OnClickListener,View.OnTouchListener{

    LinearLayout[] selections;
    View[] lines;

    final int DEVIATION = 10;
    float originY;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.more_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lines = new View[]
                {
                    findViewById(R.id.line0),findViewById(R.id.line1),findViewById(R.id.line2),findViewById(R.id.line3),
                    findViewById(R.id.line4),findViewById(R.id.line5),findViewById(R.id.line6),findViewById(R.id.line7),
                    findViewById(R.id.line8),findViewById(R.id.line9)
                };

        selections = new LinearLayout[]
                {
                    (LinearLayout) findViewById(R.id.gender_layout),(LinearLayout) findViewById(R.id.areas_layout),
                    (LinearLayout) findViewById(R.id.handwriting_layout),(LinearLayout) findViewById(R.id.tel_layout),
                    (LinearLayout) findViewById(R.id.email_layout),(LinearLayout) findViewById(R.id.name_layout),
                    (LinearLayout) findViewById(R.id.introduction_layout)
                };
        for(int i=0;i<7;i++)
        {
            selections[i].setOnClickListener(this);
            selections[i].setOnTouchListener(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();


        ((TextView)selections[1].getChildAt(1)).setText(UserInfoHelper.findUserArea(Preference.userInfoMap.get("areas")));

        ((TextView)selections[0].getChildAt(1)).setText((Preference.userInfoMap.get("gender").length()==0?"未填写":Preference.userInfoMap.get("gender")));
        ((TextView)selections[2].getChildAt(1)).setText(Preference.userInfoMap.get("handwriting").length()==0?"未填写":Preference.userInfoMap.get("handwriting"));
        ((TextView)selections[3].getChildAt(1)).setText(Preference.userInfoMap.get("tel").length()==0?"未填写":Preference.userInfoMap.get("tel"));
        ((TextView)selections[4].getChildAt(1)).setText(Preference.userInfoMap.get("email").length()==0?"未填写":Preference.userInfoMap.get("email"));
        ((TextView)selections[5].getChildAt(1)).setText(Preference.userInfoMap.get("name").length()==0?"未填写":Preference.userInfoMap.get("name"));


    }

    @Override
    protected void onResume() {
        super.onResume();

        for(int i = 0; i < 7; i++)
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
                if(view == selections[0]) lines[1].setBackgroundColor(Color.parseColor("#FFFFFF"));

                else if(view == selections[1]){
                    lines[1].setBackgroundColor(Color.parseColor("#FFFFFF"));
                    lines[2].setBackgroundColor(Color.parseColor("#FFFFFF"));
                }

                else if(view == selections[2]) lines[2].setBackgroundColor(Color.parseColor("#FFFFFF"));

                else if(view == selections[3]) lines[5].setBackgroundColor(Color.parseColor("#FFFFFF"));

                else if(view == selections[4]){
                    lines[5].setBackgroundColor(Color.parseColor("#FFFFFF"));
                    lines[6].setBackgroundColor(Color.parseColor("#FFFFFF"));
                }

                else if(view == selections[5]) lines[6].setBackgroundColor(Color.parseColor("#FFFFFF"));

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
    public void onClick(View view) {
        if(view == selections[0]) startActivity(new Intent(this, GenderActivity.class));

        else if(view == selections[1]) startActivity(new Intent(this, AreaActivity.class));

        else if(view == selections[2]) startActivity(new Intent(this, HandWritingActivity.class));

        else if(view == selections[3]) startActivity(new Intent(this, TelActivity.class));

        else if(view == selections[4]) startActivity(new Intent(this,EMailActivity.class));

        else if(view == selections[5]) startActivity(new Intent(this, NameActivity.class));

        else if(view == selections[6]) startActivity(new Intent(this, IntroductionActivity.class));
    }

    private void resetLinesColor()
    {
        lines[1].setBackgroundColor(Color.parseColor("#d9d9d9"));
        lines[2].setBackgroundColor(Color.parseColor("#d9d9d9"));
        lines[5].setBackgroundColor(Color.parseColor("#d9d9d9"));
        lines[6].setBackgroundColor(Color.parseColor("#d9d9d9"));
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
