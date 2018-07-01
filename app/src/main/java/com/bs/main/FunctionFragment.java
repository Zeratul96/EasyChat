package com.bs.main;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.bs.easy_chat.R;
import com.bs.main.adapter.FunctionAdapter;
import com.bs.function.RecommendActivity;
import com.bs.function.SearchActivity;
import com.bs.function.ShoppingActivity;
import com.bs.function.diary.DiaryActivity;
import com.bs.function.moments.MomentsActivity;
import com.bs.function.secret.SecretActivity;
import com.bs.function.notes.NotesActivity;
import com.bs.function.words.WordsActivity;
import com.bs.parameter.Preference;

/**
 * Created by 13273 on 2017/9/16.
 *
 */

public class FunctionFragment extends Fragment implements View.OnClickListener{

    View separate;
    LinearLayout[] selections;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.function_layout, container, false);
        separate = view.findViewById(R.id.separate);
        selections = new LinearLayout[]{view.findViewById(R.id.note_layout),view.findViewById(R.id.diary_layout)};
        for(int i=0;i<2;i++){
            selections[i].setOnClickListener(this);
        }

        GridView societyGridView = view.findViewById(R.id.society_grid_view);
        societyGridView.setAdapter(new FunctionAdapter(new String[]{"那一刻","悄悄话","留言板"},new int[]{R.drawable.moments,R.drawable.whisper,R.drawable.words}, MainActivity.mainActivity));
        societyGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        startActivity(new Intent(MainActivity.mainActivity, MomentsActivity.class));
                        break;

                    case 1:
                        startActivity(new Intent(MainActivity.mainActivity, SecretActivity.class));
                        break;

                    case 2:
                        Bundle bundle = new Bundle();
                        bundle.putString("user_id", Preference.userInfoMap.get("user_id"));
                        startActivity(new Intent(MainActivity.mainActivity, WordsActivity.class).putExtras(bundle));
                        break;
                }
            }
        });

        GridView linkGridView = view.findViewById(R.id.link_grid_view);
        linkGridView.setAdapter(new FunctionAdapter(new String[]{"搜一搜","看一看","购物"}, new int[]{R.drawable.search,R.drawable.recommend,R.drawable.shopping}, MainActivity.mainActivity));
        linkGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        startActivity(new Intent(MainActivity.mainActivity, SearchActivity.class));
                        break;

                    case 1:
                        startActivity(new Intent(MainActivity.mainActivity, RecommendActivity.class));
                        break;

                    case 2:
                        startActivity(new Intent(MainActivity.mainActivity, ShoppingActivity.class));
                        break;
                }
            }
        });
        return view;
    }

    @Override
    public void onClick(View v) {
        if(v == selections[0])
            startActivity(new Intent(MainActivity.mainActivity, NotesActivity.class));

        else
            startActivity(new Intent(MainActivity.mainActivity, DiaryActivity.class));
    }
}
