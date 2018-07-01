package com.bs.function.words;

import android.os.Bundle;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.bs.database.DataBaseUtil;
import com.bs.easy_chat.R;
import com.bs.main.decoration.StaggeredGridLayoutDecoration;
import com.bs.parameter.Constant;
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

/**
 *Intent:user_id
 */
public class WordsActivity extends BaseActivity implements XRecyclerView.LoadingListener{

    XRecyclerView recyclerView;
    static boolean shouldRefreshContent = false;
    WordsAdapter adapter;
    List<Map<String,String>> dataList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.words_layout);

        recyclerView = (XRecyclerView) findViewById(R.id.board);
        recyclerView.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);//刷新动画风格
        recyclerView.setLoadingMoreProgressStyle(ProgressStyle.LineScalePulseOutRapid);//加载更多风格
        recyclerView.setArrowImageView(R.drawable.ic_pulltorefresh_arrow);
        recyclerView.setLoadingListener(this);
        //布局管理
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        StaggeredGridLayoutDecoration itemDecoration = new StaggeredGridLayoutDecoration(16);
        recyclerView.addItemDecoration(itemDecoration);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new WordsAdapter(this, dataList, getIntent().getExtras().getString("user_id"));
        recyclerView.setAdapter(adapter);
        queryWords();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(shouldRefreshContent){
            refreshThread(false);
            shouldRefreshContent = false;
        }
    }

    private void queryWords(){
        dataList = DataBaseUtil.queryWordsProgressively(getIntent().getExtras().getString("user_id"), "now");
        adapter.setData(dataList);
        adapter.notifyDataSetChanged();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String,String> map = new HashMap<>();
                map.put("msgType", Constant.QUERY_WORDS_PROGRESSIVELY);
                map.put("write_to_user", getIntent().getExtras().getString("user_id"));
                map.put("range","now");
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshThread(final boolean needRefreshAnim){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String,String> map = new HashMap<>();
                map.put("msgType", Constant.REFRESH_WORDS);
                map.put("write_to_user", getIntent().getExtras().getString("user_id"));
                final String range = (dataList==null||dataList.isEmpty())?"0":dataList.get(dataList.size()-1).get("words_id");
                map.put("range", range);

                final String result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(), 0);
                MainHandler.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        //加载本地数据
                        dataList.clear();
                        List<Map<String,String>> localData = DataBaseUtil.queryWordsRefreshed(getIntent().getExtras().getString("user_id"), range);
                        for(Map<String,String> temp:localData)
                            dataList.add(temp);

                        onLoadDataResult(result, false, null);
                        if(needRefreshAnim)
                            recyclerView.refreshComplete();

                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    @Override
    public void onRefresh() {
        refreshThread(true);
    }

    @Override
    public void onLoadMore() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String,String> map = new HashMap<>();
                map.put("msgType", Constant.QUERY_WORDS_PROGRESSIVELY);
                map.put("write_to_user", getIntent().getExtras().getString("user_id"));
                final String range = (dataList==null||dataList.isEmpty())?"now":dataList.get(dataList.size()-1).get("words_id");
                map.put("range", range);

                final String result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(), 0);
                MainHandler.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        //加载更多如果没有内容不触发onLoadDataResult  注意：网络连接失败也走这条路线
                        if(!result.equals("[null]")) {
                            List<Map<String,String>> list = DataBaseUtil.queryWordsProgressively(getIntent().getExtras().getString("user_id"), range);
                            onLoadDataResult(result, true, list);
                            recyclerView.loadMoreComplete();
                        }
                        else recyclerView.setNoMore(true);

                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    /**
     * 接收数据完成
     * @param result:服务器返回数据
     * @param isContinue:是加载更多模式
     * @param localContinueData:本地的加载更多出的数据
     */
    private void onLoadDataResult(String result, boolean isContinue, List<Map<String,String>> localContinueData) {
        if (!result.equals(Constant.SERVER_CONNECTION_ERROR)) {
            if (!result.equals("[null]")){
                if(isContinue){
                    LocalDataIOUtil.syncLocalData(SQLLiteConstant.WORDS_TABLE, dataList, FastJSON.parseJSON2ListString(result), localContinueData, true);
                }
                else
                    LocalDataIOUtil.syncLocalData(SQLLiteConstant.WORDS_TABLE, dataList, FastJSON.parseJSON2ListString(result), null, false);
            }

            //也许数据发生变化原有数据都被删除了 这种情况只有初次加载或者刷新时会出现
            else
                LocalDataIOUtil.syncLocalData(SQLLiteConstant.WORDS_TABLE, dataList, new ArrayList<Map<String, String>>(), null, false);
        }
    }

}
