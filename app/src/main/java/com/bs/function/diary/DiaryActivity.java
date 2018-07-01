package com.bs.function.diary;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.bs.database.DataBaseUtil;
import com.bs.easy_chat.R;
import com.bs.parameter.Preference;
import com.bs.util.BaseActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiaryActivity extends BaseActivity{

    List<Map<String ,String >> dataList;
    ListView listView;

    private Toolbar.OnMenuItemClickListener onMenuItemClickListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()){
                case R.id.action_edit:
                    startActivity(new Intent(DiaryActivity.this, CreateDiaryActivity.class));
                    break;
            }
            return false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diary_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");
        //要写在setSupportActionBar后面才起作用
        toolbar.setOnMenuItemClickListener(onMenuItemClickListener);
        if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listView = (ListView) findViewById(R.id.list_for_diary);
    }

    @Override
    protected void onStart() {
        super.onStart();
        dataList = DataBaseUtil.queryDiary(Preference.userInfoMap.get("user_id"));

        if(Preference.isTimeLineStyle){
            TimeStyleAdapter timeStyleAdapter = new TimeStyleAdapter(dataList.size(),this);
            timeStyleAdapter.dateConvert();
            listView.setAdapter(timeStyleAdapter);
        }
        else listView.setAdapter(new DiaryAdapter(dataList.size(),this));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String ,String> piece = (HashMap<String, String>) dataList.get(position);
                startActivity(new Intent(DiaryActivity.this, CreateDiaryActivity.class).putExtra("msg", piece));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.diary_menu, menu);
        return true;
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
