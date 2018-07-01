package com.bs.person.person_center;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bs.easy_chat.R;
import com.bs.parameter.Preference;
import com.bs.util.BaseActivity;
import com.bs.util.ImageTransmissionUtil;
import com.bs.util.MainHandler;

public class PersonalCenterActivity extends BaseActivity implements View.OnTouchListener,View.OnClickListener{

    LinearLayout[] selections;
    View[] lines;
    private ImageView sculptureImage;

    final int DEVIATION = 10;
    float originY;
    byte[] picData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_center_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        selections = new LinearLayout[]
                {
                        (LinearLayout) findViewById(R.id.sculpture_layout),(LinearLayout) findViewById(R.id.nickname_layout),(LinearLayout) findViewById(R.id.account_layout),
                        (LinearLayout) findViewById(R.id.QR_layout),(LinearLayout) findViewById(R.id.more_layout)
                };
        lines = new View[]
                {
                    findViewById(R.id.line0),findViewById(R.id.line1),findViewById(R.id.line2),findViewById(R.id.line3),
                    findViewById(R.id.line4),findViewById(R.id.line5)
                };

        for(int i=0;i<5;i++){
            selections[i].setOnClickListener(this);
            selections[i].setOnTouchListener(this);
        }

        sculptureImage = (ImageView) findViewById(R.id.sculpture_image);
    }

    @Override
    protected void onStart() {
        super.onStart();

        loadSculpture();
        ((TextView)selections[1].getChildAt(1)).setText(Preference.userInfoMap.get("nickname"));
        ((TextView)selections[2].getChildAt(1)).setText(Preference.userInfoMap.get("user_id"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        for(int i=0;i<5;i++)
            selections[i].setBackgroundColor(Color.parseColor("#FFFFFF"));

        resetLinesColor();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent){
        final int TOP_Y = 0;
        final int BOTTOM_Y = view.getBottom() - view.getTop();

        switch (motionEvent.getAction())
        {
            case MotionEvent.ACTION_DOWN:

                originY = motionEvent.getY();

                if(view == selections[2])
                    return true;

                view.setBackgroundColor(Color.parseColor("#d9d9d9"));

                //lines color change
                if(view == selections[0])
                    lines[1].setBackgroundColor(Color.parseColor("#FFFFFF"));

                else if(view == selections[1]){
                    lines[1].setBackgroundColor(Color.parseColor("#FFFFFF"));
                    lines[2].setBackgroundColor(Color.parseColor("#FFFFFF"));
                }

                else if(view == selections[2]){
                    lines[2].setBackgroundColor(Color.parseColor("#FFFFFF"));
                    lines[3].setBackgroundColor(Color.parseColor("#FFFFFF"));
                }

                else if(view == selections[3]){
                    lines[3].setBackgroundColor(Color.parseColor("#FFFFFF"));
                    lines[4].setBackgroundColor(Color.parseColor("#FFFFFF"));
                }

                else if(view == selections[4])
                    lines[4].setBackgroundColor(Color.parseColor("#FFFFFF"));

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
        if(view == selections[0]){
            Intent intent = new Intent(this, SculptureActivity.class);
            if(picData != null){
                Bundle bundle = new Bundle();
                bundle.putByteArray("msg", picData);
                intent.putExtras(bundle);
            }
            startActivity(intent);
        }

        else if(view == selections[1]) startActivity(new Intent(this, NicknameActivity.class));

        else if(view == selections[4]) startActivity(new Intent(this, MoreActivity.class));
    }

    private void resetLinesColor(){
        for(int i =0 ; i < 4; i++)
            lines[i+1].setBackgroundColor(Color.parseColor("#d9d9d9"));
    }

    private void loadSculpture(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = ImageTransmissionUtil.loadSculpture(PersonalCenterActivity.this, Preference.userInfoMap.get("sculpture"));
                MainHandler.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        if(bitmap != null){
                            sculptureImage.setImageBitmap(bitmap);
                        }
                    }
                });
            }
        }).start();
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
