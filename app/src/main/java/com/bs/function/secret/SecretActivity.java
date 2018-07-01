package com.bs.function.secret;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bs.database.DataBaseUtil;
import com.bs.easy_chat.R;
import com.bs.parameter.Constant;
import com.bs.parameter.Preference;
import com.bs.parameter.SQLLiteConstant;
import com.bs.tool_package.FastJSON;
import com.bs.util.BaseActivity;
import com.bs.util.LocalDataIOUtil;
import com.bs.util.MainHandler;
import com.bs.util.NetConnectionUtil;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecretActivity extends BaseActivity implements XRecyclerView.LoadingListener{

    XRecyclerView xRecyclerView;
    static boolean shouldRefreshContent = false;

    SecretAdapter adapter;
    List<Map<String, String>> dataList;
    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {

            switch (menuItem.getItemId()){
                case R.id.action_edit:
                startActivity(new Intent(SecretActivity.this, WriteSecretActivity.class));
                break;
            }
            return true;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.secret_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");
        toolbar.setOnMenuItemClickListener(onMenuItemClick);
        if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView = (XRecyclerView) findViewById(R.id.recycler_view);
        xRecyclerView.setLayoutManager(layoutManager);

        xRecyclerView.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);//刷新动画风格
        xRecyclerView.setLoadingMoreProgressStyle(ProgressStyle.LineScalePulseOutRapid);//加载更多风格
        xRecyclerView.setArrowImageView(R.drawable.ic_pulltorefresh_arrow);
        xRecyclerView.setLoadingListener(this);

        View header = LayoutInflater.from(this).inflate(R.layout.secret_header_layout,(ViewGroup) findViewById(android.R.id.content), false);
        xRecyclerView.addHeaderView(header);

        adapter = new SecretAdapter(this, dataList);
        xRecyclerView.setAdapter(adapter);
        querySecret();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(shouldRefreshContent) {
            refreshThread(false);
            shouldRefreshContent = false;
        }
    }

    private void querySecret(){
        dataList = DataBaseUtil.querySecretProgressively("now");
        adapter.setData(dataList);
        adapter.notifyDataSetChanged();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String,String> map = new HashMap<>();
                map.put("msgType", Constant.QUERY_SECRET_PROGRESSIVELY);
                map.put("range","now");
                map.put("user_id", Preference.userInfoMap.get("user_id"));
                final String result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(), 0);
                MainHandler.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        onLoadDataResult(result, false, null);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }


    private void refreshThread(final boolean needRefreshAnim){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String,String> map = new HashMap<>();
                map.put("msgType", Constant.REFRESH_SECRET);
                final String range = (dataList==null||dataList.isEmpty())?"0":dataList.get(dataList.size()-1).get("moments_id");
                map.put("range", range);
                map.put("user_id", Preference.userInfoMap.get("user_id"));

                final String result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(), 0);
                MainHandler.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        //加载本地数据
                        dataList.clear();
                        List<Map<String,String>> localData = DataBaseUtil.querySecretRefreshed(range);
                        for(Map<String,String> temp:localData)
                            dataList.add(temp);

                        onLoadDataResult(result, false, null);
                        if(needRefreshAnim)
                            xRecyclerView.refreshComplete();

                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }


    @Override
    public void onLoadMore() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String,String> map = new HashMap<>();
                map.put("msgType", Constant.QUERY_SECRET_PROGRESSIVELY);
                map.put("user_id", Preference.userInfoMap.get("user_id"));
                final String range = (dataList==null||dataList.isEmpty())?"now":dataList.get(dataList.size()-1).get("moments_id");
                map.put("range", range);

                final String result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(), 0);
                MainHandler.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        //加载本地数据
                        List<Map<String,String>> list = DataBaseUtil.querySecretProgressively(range);
                        //加载更多如果没有内容不触发onLoadDataResult  注意：网络连接失败也走这条路线
                        if(!result.equals("[null]")) {
                            onLoadDataResult(result, true, list);
                            xRecyclerView.loadMoreComplete();
                        }
                        else xRecyclerView.setNoMore(true);

                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    @Override
    public void onRefresh() {refreshThread(true);}

    private void onLoadDataResult(String result, boolean isContinue, List<Map<String,String>> localContinueData) {
        if (!result.equals(Constant.SERVER_CONNECTION_ERROR)) {
            if (!result.equals("[null]")){
                if(isContinue){
                    LocalDataIOUtil.syncLocalData(SQLLiteConstant.SECRET_TABLE, dataList, FastJSON.parseJSON2ListString(result), localContinueData, true);
                }
                else
                    LocalDataIOUtil.syncLocalData(SQLLiteConstant.SECRET_TABLE, dataList, FastJSON.parseJSON2ListString(result), null, false);
            }

            //也许数据发生变化原有数据都被删除了 这种情况只有初次加载或者刷新时会出现
            else
                LocalDataIOUtil.syncLocalData(SQLLiteConstant.SECRET_TABLE, dataList, new ArrayList<Map<String, String>>(), null, false);
        }
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
        getMenuInflater().inflate(R.menu.secret_menu, menu);
        return true;
    }

}
