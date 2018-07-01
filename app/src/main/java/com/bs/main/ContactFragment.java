package com.bs.main;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.bs.database.DataBaseUtil;
import com.bs.easy_chat.R;
import com.bs.main.adapter.ContactAdapter;
import com.bs.main.bean.ContactBean;
import com.bs.main.decoration.DividerItemDecoration;
import com.bs.util.NetConnectionUtil;
import com.bs.util.chat_util.ClientAgentThread;
import com.mcxtzhang.indexlib.IndexBar.widget.IndexBar;
import com.mcxtzhang.indexlib.suspension.SuspensionDecoration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 13273 on 2017/9/16.
 *
 */

public class ContactFragment extends Fragment implements TextWatcher{

    private View view;
    private static final String INDEX_STRING_TOP = "Top";

    ContactAdapter contactAdapter;
    private SuspensionDecoration mDecoration;

    private IndexBar mIndexBar;
    private EditText editText;
    private TextView noResultTextView;
    /**
     * originData中map格式:friend_id,name.sculpture
     */
    private List<Map<String, String>> originData = new ArrayList<>();
    private List<ContactBean> objectData = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.contact_layout, container, false);
        this.view = view;

        editText = view.findViewById(R.id.search_bar);
        editText.addTextChangedListener(this);
        RecyclerView recyclerView = view.findViewById(R.id.rv);
        TextView mTvSideBarHint = view.findViewById(R.id.tvSideBarHint);
        mIndexBar = view.findViewById(R.id.indexBar);
        noResultTextView = view.findViewById(R.id.no_result);
        noResultTextView.setVisibility(View.GONE);
        mIndexBar.setVisibility(View.VISIBLE);

        //设置布局
        LinearLayoutManager mManager = new LinearLayoutManager(MainActivity.mainActivity);
        recyclerView.setLayoutManager(mManager);

        //设置适配器
        contactAdapter = new ContactAdapter(MainActivity.mainActivity, originData);
        recyclerView.setAdapter(contactAdapter);

        //装饰
        recyclerView.addItemDecoration(mDecoration = new SuspensionDecoration(MainActivity.mainActivity, objectData));
        recyclerView.addItemDecoration(new DividerItemDecoration(MainActivity.mainActivity, DividerItemDecoration.VERTICAL_LIST));

        //indexBar初始化
        mIndexBar.setmPressedShowTextView(mTvSideBarHint)
                .setNeedRealIndex(false)
                .setmLayoutManager(mManager);
        initAndRefreshView(DataBaseUtil.queryFriends(), true);

        //添加好友监听回调
        NetConnectionUtil.cat.setOnAddNewFriendListener(new ClientAgentThread.OnFriendChangedListener() {
            @Override
            public void onFriendChanged() {
                initAndRefreshView(DataBaseUtil.queryFriends(), true);
            }
        });

        return view;
    }

    /**
     *
     * @param contactData:联系人数据
     * @param isNeedSystemIcons:是否需要两个系统图标
     */
    private void initAndRefreshView(List<Map<String,String>> contactData ,boolean isNeedSystemIcons){
        originData.clear();
        objectData.clear();

        if(isNeedSystemIcons){
            Map<String ,String> addFriends = new HashMap<>();
            Map<String ,String> chatGroup = new HashMap<>();
            addFriends.put("name","新的朋友");
            addFriends.put("friend_id", "");

            chatGroup.put("name","群聊");
            chatGroup.put("friend_id","");

            originData.add(addFriends);
            originData.add(chatGroup);
            objectData.add((ContactBean) new ContactBean("新的朋友").setTop(true).setBaseIndexTag(INDEX_STRING_TOP));
            objectData.add((ContactBean) new ContactBean("群聊").setTop(true).setBaseIndexTag(INDEX_STRING_TOP));
        }

        for(Map<String,String> piece:contactData){
            HashMap<String,String> map = new HashMap<>();
            String name = piece.get("remark_name").length()==0?piece.get("nickname"):piece.get("remark_name");
            map.put("name", name);
            map.put("friend_id", piece.get("friend_id"));
            map.put("sculpture", piece.get("sculpture"));
            originData.add(map);
            objectData.add(new ContactBean(name));
        }

        contactAdapter.notifyDataSetChanged();
        mIndexBar.setmSourceDatas(objectData)//设置数据
                .invalidate();
        mDecoration.setmDatas(objectData);
    }

    public void cancelEditFocusAndResetUI(){
        view.findViewById(R.id.search_bar).clearFocus();
        editText.setText("");
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void afterTextChanged(Editable s) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(editText.getText().toString().length()>0){
            List<Map<String,String>> resultList = DataBaseUtil.queryFriendsByCondition("%"+editText.getText().toString()+"%");
            if(resultList.isEmpty())
                noResultTextView.setVisibility(View.VISIBLE);

            mIndexBar.setVisibility(View.GONE);
            initAndRefreshView(resultList,false);
        }
        else{
            initAndRefreshView(DataBaseUtil.queryFriends(),true);
            noResultTextView.setVisibility(View.GONE);
            mIndexBar.setVisibility(View.VISIBLE);
        }
    }
}
