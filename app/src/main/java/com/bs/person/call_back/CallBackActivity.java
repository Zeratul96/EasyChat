package com.bs.person.call_back;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.bs.database.DataBaseUtil;
import com.bs.easy_chat.R;
import com.bs.function.comment.CommentActivity;
import com.bs.parameter.Constant;
import com.bs.parameter.Preference;
import com.bs.parameter.SQLLiteConstant;
import com.bs.tool_package.FastJSON;
import com.bs.util.BaseActivity;
import com.bs.util.LocalDataIOUtil;
import com.bs.util.MainHandler;
import com.bs.util.NetConnectionUtil;

import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 13273 on 2017/11/5.
 *
 */

public class CallBackActivity extends BaseActivity implements CallBackAdapter.OnTouchContentListener{

    private CallBackAdapter adapter;
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
                        map.put("msgType", Constant.CLEAR_CALLBACK);
                        final String result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(), 0);
                        MainHandler.getInstance().post(new Runnable() {
                            @Override
                            public void run() {
                                if(result.equals(Constant.SERVER_CONNECTION_ERROR)||result.equals("[null]"))
                                    Toast.makeText(CallBackActivity.this, getText(R.string.no_network), Toast.LENGTH_SHORT).show();
                                else{
                                    dataList.clear();
                                    DataBaseUtil.delete("delete from callback", SQLLiteConstant.CALL_BACK);
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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");
        toolbar.setOnMenuItemClickListener(onMenuItemClick);
        if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new CallBackAdapter(this,dataList);
        adapter.setOnTouchContentListener(this);

        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        dataList = DataBaseUtil.queryCallBack(Preference.userInfoMap.get("user_id"));
        adapter.setData(dataList).notifyDataSetChanged();
        queryCallBack();
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

    private void queryCallBack(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String,String> hashMap = new HashMap<>();
                hashMap.put("msgType", Constant.QUERY_CALLBACK);
                hashMap.put("user_id", Preference.userInfoMap.get("user_id"));
                final String result = NetConnectionUtil.uploadData(JSONObject.fromObject(hashMap).toString(), 0);
                MainHandler.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        onLoadDataResult(result);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    @Override
    public void onTouchContent(String momentsID, String authorID) {
        Bundle bundle = new Bundle();
        bundle.putString("msg",momentsID);
        bundle.putString("author", authorID);
        startActivity(new Intent(this, CommentActivity.class).putExtras(bundle));
    }

    private void onLoadDataResult(String result) {
        if (!result.equals(Constant.SERVER_CONNECTION_ERROR)) {
            if (!result.equals("[null]")){
                List<Map<String,String>> serverData = FastJSON.parseJSON2ListString(result);
                LocalDataIOUtil.syncLocalData(SQLLiteConstant.CALL_BACK, dataList, serverData, null, false);
            }
            //也许数据发生变化原有数据都被删除了 这种情况只有初次加载或者刷新时会出现
            else
                LocalDataIOUtil.syncLocalData(SQLLiteConstant.CALL_BACK, dataList, new ArrayList<Map<String, String>>(), null, false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.call_back_menu, menu);
        return true;
    }
}
