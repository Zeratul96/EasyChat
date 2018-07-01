package com.bs.function.moments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.bs.database.DataBaseUtil;
import com.bs.easy_chat.R;
import com.bs.function.comment.CommentActivity;
import com.bs.parameter.Constant;
import com.bs.parameter.Preference;
import com.bs.parameter.SQLLiteConstant;
import com.bs.util.BaseActivity;
import com.bs.util.MainHandler;
import com.bs.util.NetConnectionUtil;

import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MsgActivity extends BaseActivity implements MsgAdapter.OnTouchContentListener{

    private MsgAdapter adapter;
    private List<Map<String,String>> dataList;
    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()){
                case R.id.action_clear_all:
                    if(dataList == null || dataList.isEmpty()) return true;

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            HashMap<String,String> map = new HashMap<>();
                            map.put("mode","1");
                            map.put("user_id", Preference.userInfoMap.get("user_id"));
                            map.put("msgType", Constant.CLEAR_MSG);
                            final String result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(), 0);
                            MainHandler.getInstance().post(new Runnable() {
                                @Override
                                public void run() {
                                    if(result.equals(Constant.SERVER_CONNECTION_ERROR)||result.equals("[null]"))
                                        Toast.makeText(MsgActivity.this, getText(R.string.no_network), Toast.LENGTH_SHORT).show();
                                    else{
                                        dataList.clear();
                                        DataBaseUtil.delete("delete from msg", SQLLiteConstant.MSG_TABLE);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                    }).start();

                    break;
            }
            return true;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call_back_layout);

        MomentsActivity.shouldRefreshContent = true;
        ((TextView)findViewById(R.id.title_name)).setText("新消息");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");
        toolbar.setOnMenuItemClickListener(onMenuItemClick);
        if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        dataList = (ArrayList<Map<String,String>>)getIntent().getSerializableExtra("msg");
        adapter = new MsgAdapter(this,dataList);
        adapter.setOnTouchContentListener(this);

        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onTouchContent(String momentsID, String authorID, final String commentID) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                //删除消息
                HashMap<String,String> hashMap = new HashMap<>();
                hashMap.put("msgType", Constant.CLEAR_MSG);
                hashMap.put("mode", "0");
                hashMap.put("comment_id", commentID);
                String result = NetConnectionUtil.uploadData(JSONObject.fromObject(hashMap).toString(), 0);
                if(!result.equals(Constant.SERVER_CONNECTION_ERROR) && !result.equals("[null]"))
                    DataBaseUtil.delete("delete from msg where comment_id = '"+hashMap.get("comment_id")+"'", SQLLiteConstant.MSG_TABLE);

            }
        }).start();

        Bundle bundle = new Bundle();
        bundle.putString("msg",momentsID);
        bundle.putString("author", authorID);
        startActivity(new Intent(this, CommentActivity.class).putExtras(bundle));
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
        getMenuInflater().inflate(R.menu.call_back_menu, menu);
        return true;
    }
}
