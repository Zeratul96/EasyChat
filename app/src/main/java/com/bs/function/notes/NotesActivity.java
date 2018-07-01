package com.bs.function.notes;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.bs.database.DataBaseUtil;
import com.bs.easy_chat.R;
import com.bs.parameter.Preference;
import com.bs.parameter.SQLLiteConstant;
import com.bs.util.BaseActivity;
import com.bs.widget.SwipeListView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotesActivity extends BaseActivity {

    SwipeListView listView;
    List<Map<String,String>> dataList;
    private Toolbar.OnMenuItemClickListener onMenuItemClickListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()){
                case R.id.action_edit:
                    startActivity(new Intent(NotesActivity.this, CreateNotesActivity.class));
                    break;
            }
            return false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notes_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");
        if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setOnMenuItemClickListener(onMenuItemClickListener);

        listView = (SwipeListView) findViewById(R.id.list_for_notes);
    }

    @Override
    protected void onStart() {
        super.onStart();

        dataList =  DataBaseUtil.queryNotes(Preference.userInfoMap.get("user_id"));
        listView.setVisibility(View.VISIBLE);
        final NotesAdapter notesAdapter = new NotesAdapter(this, dataList, listView.getRightViewWidth());
        notesAdapter.setOnRightItemClickListener(new NotesAdapter.onRightItemClickListener() {
            @Override
            public void onRightItemClick(View v, int position) {
                deleteNotes(position);
                dataList.remove(position);
                listView.shrinkByDeletion();
                notesAdapter.notifyDataSetChanged();
            }
        });
        listView.setAdapter(notesAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    HashMap<String ,String> msgMap = (HashMap<String, String>) dataList.get(position);
                    startActivity(new Intent(NotesActivity.this, CreateNotesActivity.class).putExtra("msg",msgMap));
            }
        });
    }

    private void deleteNotes(int i){
        String sql = "delete from notes where primary_key = '"+dataList.get(i).get("primary_key")+"'";
        DataBaseUtil.delete(sql, SQLLiteConstant.NOTES_TABLE);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.notes_menu, menu);
        return true;
    }
}
