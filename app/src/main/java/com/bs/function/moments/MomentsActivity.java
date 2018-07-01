package com.bs.function.moments;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.holder.Holder;
import com.bs.database.DataBaseUtil;
import com.bs.easy_chat.R;
import com.bs.parameter.Constant;
import com.bs.parameter.Preference;
import com.bs.parameter.SQLLiteConstant;
import com.bs.tool_package.FastJSON;
import com.bs.tool_package.ImageTools;
import com.bs.util.BaseActivity;
import com.bs.util.ImageTransmissionUtil;
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

public class MomentsActivity extends BaseActivity implements XRecyclerView.LoadingListener{

    ConvenientBanner convenientBanner;
    View header;
    static boolean shouldRefreshContent = false;
    Menu menu;

    XRecyclerView xRecyclerView;
    MomentsAdapter adapter;
    List<Map<String,String>> dataList;
    List<Map<String,String>> favoriteList;
    ArrayList<Map<String,String>> msgList;
    List<Map<String,String>> recommendList;
    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()){
                case R.id.action_edit:
                    startActivity(new Intent(MomentsActivity.this, CreateMomentsActivity.class));
                    break;

                case R.id.action_message:
                    startActivity(new Intent(MomentsActivity.this, MsgActivity.class).putExtra("msg", msgList));
                    break;
            }
            return true;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.moments_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");
        toolbar.setOnMenuItemClickListener(onMenuItemClick);
        if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        xRecyclerView = (XRecyclerView) findViewById(R.id.recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(layoutManager);

        xRecyclerView.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);//刷新动画风格
        xRecyclerView.setLoadingMoreProgressStyle(ProgressStyle.LineScalePulseOutRapid);//加载更多风格
        xRecyclerView.setArrowImageView(R.drawable.ic_pulltorefresh_arrow);
        xRecyclerView.setLoadingListener(this);

        header = LayoutInflater.from(this).inflate(R.layout.moments_header_layout,(ViewGroup)findViewById(android.R.id.content), false);
        convenientBanner = header.findViewById(R.id.convenient_banner);
        xRecyclerView.addHeaderView(header);
        ((SimpleItemAnimator)xRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        adapter = new MomentsAdapter(this, dataList);
        xRecyclerView.setAdapter(adapter);
        queryMoments();
    }


    @Override
    protected void onStart() {
        super.onStart();
        //每次打开这个界面从本地数据库加载一次消息情况
        msgList = (ArrayList<Map<String,String>>) DataBaseUtil.queryMsg();
        if(menu != null)
            menu.findItem(R.id.action_message).setIcon(msgList.isEmpty()?R.mipmap.no_message_icon:R.mipmap.has_message_icon);
        
        if(shouldRefreshContent) {
            refreshThread(false);
            shouldRefreshContent = false;
        }
        else{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    loadMsgList();
                }
            }).start();
        }
    }

    @Override
    public void onRefresh() {refreshThread(true);}

    @Override
    public void onLoadMore() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                loadMsgList();
                Map<String,String> map = new HashMap<>();
                map.put("msgType", Constant.QUERY_MOMENTS_PROGRESSIVELY);
                map.put("user_id", Preference.userInfoMap.get("user_id"));
                final String range = (dataList==null||dataList.isEmpty())?"now":dataList.get(dataList.size()-1).get("moments_id");
                map.put("range", range);

                final String result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(), 0);
                MainHandler.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        //加载本地数据
                        List<Map<String,String>> list = DataBaseUtil.queryMomentsProgressively(range);
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

    private void queryMoments(){
        //快速初始化界面
        dataList = DataBaseUtil.queryMomentsProgressively("now");
        adapter.setData(dataList);
        favoriteList = DataBaseUtil.queryFavorite();
        adapter.setFavoriteList(favoriteList);
        adapter.notifyDataSetChanged();

        recommendList = DataBaseUtil.queryRecommend();
        showConvenientBanner(recommendList);

        new Thread(new Runnable() {
            @Override
            public void run() {
                loadSelfFavoriteInfo();
                Map<String,String> map = new HashMap<>();
                map.put("msgType", Constant.QUERY_MOMENTS_PROGRESSIVELY);
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
                //加载推荐栏
                queryRecommendMoments();
            }
        }).start();
    }

    private void refreshThread(final boolean needRefreshAnim){
        new Thread(new Runnable() {
            @Override
            public void run() {
                loadSelfFavoriteInfo();
                loadMsgList();
                Map<String,String> map = new HashMap<>();
                map.put("msgType", Constant.REFRESH_MOMENTS);
                final String range = (dataList==null||dataList.isEmpty())?"0":dataList.get(dataList.size()-1).get("moments_id");
                map.put("range", range);
                map.put("user_id", Preference.userInfoMap.get("user_id"));
                final String result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(), 0);

                MainHandler.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        //加载本地数据
                        dataList.clear();
                        List<Map<String,String>> localData = DataBaseUtil.queryMomentsRefreshed(range);
                        for(Map<String,String> temp:localData)
                            dataList.add(temp);

                        onLoadDataResult(result, false, null);
                        if(needRefreshAnim)
                            xRecyclerView.refreshComplete();
                        adapter.notifyDataSetChanged();
                    }
                });
                //加载推荐栏
                queryRecommendMoments();
            }
        }).start();
    }

    /**
     * 加载一批量获得的信息的图片 累计五张加载失败便直接退出
     */
    private void loadAndSaveImage(List<Map<String,String>> tempData){
        int errorCount = 0;
        if(tempData == null || tempData.isEmpty()) return;
        for(Map<String,String> map:tempData){
            String picPaths = map.get("pictures_id");
            if(picPaths.length()>0){
                for(String s:picPaths.split("<#>")){
                    if(!ImageTransmissionUtil.loadMomentsSculptureToLocal(this,s)) errorCount++;
                    if(errorCount>=5) {
                        MainHandler.getInstance().post(new Runnable() {
                            @Override
                            public void run() {adapter.notifyDataSetChanged();
                            }
                        });
                        return;
                    }
                }
                MainHandler.getInstance().post(new Runnable() {
                    @Override
                    public void run() {adapter.notifyDataSetChanged();}
                });
            }
        }
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
                final List<Map<String,String>> serverData = FastJSON.parseJSON2ListString(result);
                if(isContinue){
                    //加载本地数据
                    LocalDataIOUtil.syncLocalData(SQLLiteConstant.MOMENTS_TABLE, dataList, serverData, localContinueData, true);
                }
                else
                    LocalDataIOUtil.syncLocalData(SQLLiteConstant.MOMENTS_TABLE, dataList, serverData, null, false);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        loadAndSaveImage(serverData);
                    }
                }).start();
            }

            //也许数据发生变化原有数据都被删除了 这种情况只有初次加载或者刷新时会出现
            else
                LocalDataIOUtil.syncLocalData(SQLLiteConstant.MOMENTS_TABLE, dataList, new ArrayList<Map<String, String>>(), null, false);
        }
    }


    /**
     * 接收数据完成
     * @param result:服务器返回数据
     */
    private void onLoadFavoriteDataResult(String result, List<Map<String,String>> localData) {
        if (!result.equals(Constant.SERVER_CONNECTION_ERROR)) {
            if (!result.equals("[null]")){
                final List<Map<String,String>> serverData = FastJSON.parseJSON2ListString(result);
                LocalDataIOUtil.syncLocalData(SQLLiteConstant.FAVORITE_TABLE, localData, serverData, null, false);
            }
            //也许数据发生变化原有数据都被删除了 这种情况只有初次加载或者刷新时会出现
            else
                LocalDataIOUtil.syncLocalData(SQLLiteConstant.FAVORITE_TABLE, localData, new ArrayList<Map<String, String>>(), null, false);
        }
    }

    /**
     * 加载推荐
     * @param result:数据
     * @param localData:本地数据
     */
    private void onLoadRecommendResult(String result, final List<Map<String,String>> localData) {
        if (!result.equals(Constant.SERVER_CONNECTION_ERROR)) {
            if (!result.equals("[null]")) {
                final List<Map<String, String>> serverData = FastJSON.parseJSON2ListString(result);
                LocalDataIOUtil.syncLocalData(SQLLiteConstant.RECOMMEND_TABLE, localData, serverData, null, false);
                //加载图片
                for (Map<String, String> map : localData) {
                    String[] s = map.get("pictures_id").split("<#>");
                    ImageTransmissionUtil.loadMomentsSculptureToLocal(MomentsActivity.this, s[0]);
                }
            }
            //也许数据发生变化原有数据都被删除了 这种情况只有初次加载或者刷新时会出现
            else
                LocalDataIOUtil.syncLocalData(SQLLiteConstant.RECOMMEND_TABLE, localData, new ArrayList<Map<String, String>>(), null, false);
        }
    }


    /**
     * 加载消息
     * @param result:服务器数据
     * @param localData:本地数据
     */
    private void onLoadMsgResult(String result, final List<Map<String,String>> localData) {
        if (!result.equals(Constant.SERVER_CONNECTION_ERROR)) {
            if (!result.equals("[null]")) {
                final List<Map<String, String>> serverData = FastJSON.parseJSON2ListString(result);
                //过滤自己的消息
                filterSelfComment(serverData);
                LocalDataIOUtil.syncLocalData(SQLLiteConstant.MSG_TABLE, localData, serverData, null, false);
            }
            //也许数据发生变化原有数据都被删除了 这种情况只有初次加载或者刷新时会出现
            else
                LocalDataIOUtil.syncLocalData(SQLLiteConstant.MSG_TABLE, localData, new ArrayList<Map<String, String>>(), null, false);
        }
    }



    /**
     * 加载自己喜欢的朋友圈动态
     */
    private void loadSelfFavoriteInfo(){
        Map<String,String> map = new HashMap<>();
        map.put("msgType", Constant.QUERY_SELF_FAVORITE);
        map.put("user_id", Preference.userInfoMap.get("user_id"));
        final String result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(), 0);

        MainHandler.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onLoadFavoriteDataResult(result, favoriteList);
            }
        });
    }


    /**
     * 加载消息列表
     */
    private void loadMsgList(){
        Map<String,String> map = new HashMap<>();
        map.put("msgType", Constant.QUERY_MSG);
        map.put("user_id", Preference.userInfoMap.get("user_id"));
        final String result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(), 0);

        MainHandler.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onLoadMsgResult(result, msgList);
                if(menu != null)
                    menu.findItem(R.id.action_message).setIcon(msgList.isEmpty()?R.mipmap.no_message_icon:R.mipmap.has_message_icon);
            }
        });
    }

    private void filterSelfComment(List<Map<String,String>> serverData){
        if(serverData.isEmpty()) return;
        List<Map<String,String>> toRemove = new ArrayList<>();
        for(Map<String,String> map:serverData){
            if(map.get("user_id").equals(Preference.userInfoMap.get("user_id")))
                toRemove.add(map);
        }
        for(Map<String,String> delete:toRemove)
            serverData.remove(delete);

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
        this.menu = menu;
        getMenuInflater().inflate(R.menu.moments_menu, menu);
        return true;
    }

    /**
     * ConvenientBanner
     */
    private void showConvenientBanner(@NonNull List<Map<String,String>> data){
        if(!data.isEmpty()){
            header.findViewById(R.id.convenient_banner).setVisibility(View.VISIBLE);
            header.findViewById(R.id.no_recommend).setVisibility(View.GONE);
            initConvenientBanner(data);
        }
        else{
            header.findViewById(R.id.convenient_banner).setVisibility(View.GONE);
            header.findViewById(R.id.no_recommend).setVisibility(View.VISIBLE);
        }
    }

    private void queryRecommendMoments(){
        final Map<String,String> mapUpload = new HashMap<>();
        mapUpload.put("msgType",Constant.QUERY_RECOMMEND_MOMENTS);
        mapUpload.put("user_id",Preference.userInfoMap.get("user_id"));

        final String result = NetConnectionUtil.uploadData(JSONObject.fromObject(mapUpload).toString(), 0);
        onLoadRecommendResult(result, recommendList);
        MainHandler.getInstance().post(new Runnable() {
            @Override
            public void run() {
                showConvenientBanner(recommendList);
            }
        });
    }

    private void initConvenientBanner(final List<Map<String,String>> dataList){
        List<Integer> sizeList = new ArrayList<>();
        for(int i=0,len = dataList.size();i<len;i++)
            sizeList.add(i);

        convenientBanner.setPages(new CBViewHolderCreator<ConvenientViewHolder>() {
            @Override
            public ConvenientViewHolder createHolder(){
                return new ConvenientViewHolder(dataList);
            }
        }, sizeList)
        .setPointViewVisible(true)
        .setPageIndicator(new int[]{R.drawable.d1, R.drawable.d2})
        .setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.CENTER_HORIZONTAL)
        .startTurning(5000)
        .setManualPageable(true);
    }

    private class ConvenientViewHolder implements Holder<Integer>{

        private View contentView;
        private ViewHolder holder;
        private List<Map<String,String>> dataList;
        private List<Map<String,String>> userInfoList;

        ConvenientViewHolder(List<Map<String,String>> data){
            this.dataList = data;
            userInfoList = DataBaseUtil.queryFriends();
        }

        @Override
        public View createView(Context context) {
            contentView = LayoutInflater.from(context).inflate(R.layout.convenient_banner, (ViewGroup)findViewById(android.R.id.content), false);
            holder = new ViewHolder(contentView);
            return contentView;
        }

        @Override
        public void UpdateUI(Context context, int position, Integer data) {
            if(dataList.isEmpty()) return;
            HashMap<String,String> map = (HashMap<String,String>) dataList.get(data);
            holder.image.setImageBitmap(ImageTransmissionUtil.loadMomentsSculptureOnlyLocal(context, map.get("pictures_id").split("<#>")[0]));
            holder.likeNum.setText(map.get("favorite_number")+"人喜欢");
            String[] infoPiece = map.get("detail_time").split("-");
            holder.time.setText(infoPiece[3]+":"+infoPiece[4]);

            String name = map.get("user_id");
            //是自己
            if(name.equals(Preference.userInfoMap.get("user_id"))) {
                name = Preference.userInfoMap.get("nickname");
                holder.sculpture.setImageBitmap(ImageTools.createCircleImage(ImageTransmissionUtil.loadSculptureOnlyInLocal(context,Preference.userInfoMap.get("sculpture"), true)));
            }
            //是好友
            else{
                Map<String, String> userInfo = findUserInfo(map.get("user_id"));
                if(userInfo != null){
                    name = userInfo.get("remark_name").length() > 0 ? userInfo.get("remark_name") : userInfo.get("nickname");
                    holder.sculpture.setImageBitmap(ImageTools.createCircleImage(ImageTransmissionUtil.loadSculptureOnlyInLocal(context, userInfo.get("sculpture"),true)));
                }
                else holder.sculpture.setImageBitmap(ImageTools.createCircleImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.no_picture)));
            }
            holder.name.setText(name);
        }

        private Map<String,String> findUserInfo(@NonNull String userID){

            for (Map<String,String> map : userInfoList){
                if(map.get("friend_id").equals(userID)){
                    return map;
                }
            }
            return null;
        }
    }

    private class ViewHolder{
        ImageView sculpture;
        TextView name;
        TextView time;
        TextView likeNum;
        ImageView image;
        ViewHolder(View view){
            sculpture = view.findViewById(R.id.sculpture);
            name = view.findViewById(R.id.name);
            time = view.findViewById(R.id.time);
            likeNum = view.findViewById(R.id.like_num);
            image = view.findViewById(R.id.image);
        }
    }
}
