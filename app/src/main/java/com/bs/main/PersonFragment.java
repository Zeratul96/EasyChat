package com.bs.main;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bs.easy_chat.R;
import com.bs.parameter.Preference;
import com.bs.person.album.PersonAlbumActivity;
import com.bs.person.call_back.CallBackActivity;
import com.bs.person.person_center.PersonalCenterActivity;
import com.bs.person.settings.SettingsActivity;
import com.bs.tool_package.ImageTools;
import com.bs.util.ImageTransmissionUtil;
import com.bs.util.MainHandler;

/**
 * Created by 13273 on 2017/9/16.
 *
 */

public class PersonFragment extends Fragment implements View.OnClickListener,View.OnTouchListener{

    LinearLayout[] selections;
    View separate;
    private int originY;

    TextView nickname;
    TextView id;
    ImageView sculptureImageView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.person_layout, container, false);

        selections = new LinearLayout[]
                {view.findViewById(R.id.album_layout),view.findViewById(R.id.comments_layout),view.findViewById(R.id.settings_layout)};
        for(int i=0;i<3;i++){
            selections[i].setOnClickListener(this);
            selections[i].setOnTouchListener(this);
        }
        separate = view.findViewById(R.id.line1);

        nickname = view.findViewById(R.id.nickname);
        id = view.findViewById(R.id.id);
        sculptureImageView = view.findViewById(R.id.sculpture_image);
        sculptureImageView.setImageBitmap(ImageTools.createCircleImage(BitmapFactory.decodeResource(getResources(),R.drawable.no_picture)));

        nickname.setOnClickListener(this);
        id.setOnClickListener(this);
        sculptureImageView.setOnClickListener(this);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadSculpture();
        if(Preference.userInfoMap != null){
            nickname.setText(Preference.userInfoMap.get("nickname"));
            id.setText("易聊号： "+Preference.userInfoMap.get("user_id"));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        for(int i=0;i<3;i++)
            selections[i].setBackgroundColor(Color.parseColor("#FFFFFF"));
        separate.setBackgroundColor(Color.parseColor("#D9D9D9"));
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent){
        final int TOP_Y = 0;
        final int BOTTOM_Y = view.getBottom() - view.getTop();

        switch (motionEvent.getAction()){
            case MotionEvent.ACTION_DOWN:

                originY = (int)motionEvent.getY();
                view.setBackgroundColor(Color.parseColor("#d9d9d9"));
                if(view == selections[0] || view ==selections[1])
                    separate.setBackgroundColor(Color.parseColor("#FFFFFF"));

                break;

            case MotionEvent.ACTION_UP:

            case MotionEvent.ACTION_MOVE:
                if(motionEvent.getY() < TOP_Y|| motionEvent.getY() > BOTTOM_Y || Math.abs(originY-motionEvent.getY()) > 10){
                    separate.setBackgroundColor(Color.parseColor("#D9D9D9"));
                    view.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    return true;
                }
                break;
        }
        return false;
    }

    @Override
    public void onClick(View view){
        if(view == selections[0]){
            Bundle bundle = new Bundle();
            bundle.putString("user_id", Preference.userInfoMap.get("user_id"));
            bundle.putString("title_name", "相册");
            bundle.putString("moments_background", Preference.userInfoMap.get("moments_background"));
            startActivity(new Intent(MainActivity.mainActivity, PersonAlbumActivity.class).putExtras(bundle));
        }

        else if(view == selections[1])
            startActivity(new Intent(MainActivity.mainActivity, CallBackActivity.class));

        else if(view == selections[2])
            startActivity(new Intent(MainActivity.mainActivity, SettingsActivity.class));

        else
            startActivity(new Intent(MainActivity.mainActivity, PersonalCenterActivity.class));
    }


    /**
     * 加载头像
     */
    public void loadSculpture(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = ImageTransmissionUtil.loadSculpture(MainActivity.mainActivity, Preference.userInfoMap.get("sculpture"));
                MainHandler.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        if(bitmap != null)
                            sculptureImageView.setImageBitmap(ImageTools.createCircleImage(bitmap));
                    }
                });
            }
        }).start();
    }

}
