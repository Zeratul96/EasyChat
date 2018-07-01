package com.bs.function.comment;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

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

import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentActivity extends BaseActivity implements CommentAdapter.OnTouchContentListener{

    /**
     * 需要的Bundle：
     * msg:moments_id
     * author
     */

    LinearLayout writeLayout;
    CommentAdapter adapter;
    RecyclerView recyclerView;
    Button sendBtn;
    EditText commentContent;

    private boolean isParent;
    String tempParentID;
    String tempObjectUser;
    List<Map<String,String>> dataList;
    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()){
                case R.id.action_edit:
                    writeLayout.setVisibility(View.VISIBLE);
                    //弹出软键盘
                    commentContent.requestFocus();
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(commentContent ,InputMethodManager.SHOW_IMPLICIT);

                    isParent = true;
                break;
            }
            return true;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comments_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");
        toolbar.setOnMenuItemClickListener(onMenuItemClick);
        if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        adapter = new CommentAdapter(this, dataList, getIntent().getExtras().getString("author"));
        adapter.setOnTouchContentListener(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        writeLayout = (LinearLayout) findViewById(R.id.write_comment_layout);

        sendBtn = (Button) findViewById(R.id.send_msg);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isParent)
                    insertComment(getIntent().getExtras().getString("msg"), null, getIntent().getExtras().getString("author"), commentContent.getText().toString());
                else
                    insertComment(getIntent().getExtras().getString("msg"), tempParentID, tempObjectUser, commentContent.getText().toString());
            }
        });
        commentContent = (EditText) findViewById(R.id.text_edit);
        commentContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sendBtn.setEnabled(commentContent.getText().toString().length()>0);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        dataList = DataBaseUtil.queryComment(getIntent().getExtras().getString("msg"));
        adapter.setData(dataList).notifyDataSetChanged();
        queryComments();
    }

    /**
     * 查询评论
     */
    private void queryComments(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String,String> map = new HashMap<>();
                map.put("msgType", Constant.QUERY_COMMENT);
                map.put("moments_id", getIntent().getExtras().getString("msg"));
                map.put("user_id", Preference.userInfoMap.get("user_id"));
                final String result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(), 0);
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

    private void insertComment(final String momentID, final String parentID, final String objectUser, final String content){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String,String> map = new HashMap<>();
                map.put("msgType", Constant.INSERT_COMMENT);
                map.put("moments_id", momentID);
                if(!isParent) map.put("parent_id", parentID);
                map.put("user_id", Preference.userInfoMap.get("user_id"));
                map.put("object_user", objectUser);
                map.put("content", content);

               String result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(), 0);
                if(!result.equals(Constant.SERVER_CONNECTION_ERROR) && !result.equals("[null]"))
                {
                    MainHandler.getInstance().post(new Runnable() {
                        @Override
                        public void run() {
                            //收缩软键盘
                            writeLayout.setVisibility(View.GONE);
                            commentContent.setText("");
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                        }
                    });
                    queryComments();
                }
            }
        }).start();
    }

    private void onLoadDataResult(String result) {
        if (!result.equals(Constant.SERVER_CONNECTION_ERROR)) {
            if (!result.equals("[null]")){
                List<Map<String,String>> serverData = FastJSON.parseJSON2ListString(result);
                for(Map<String,String> map:serverData)
                    map.put("author_id", getIntent().getExtras().getString("author"));

                LocalDataIOUtil.syncLocalData(SQLLiteConstant.COMMENT_TABLE, dataList, serverData, null, false);
            }
            //也许数据发生变化原有数据都被删除了 这种情况只有初次加载或者刷新时会出现
            else
                LocalDataIOUtil.syncLocalData(SQLLiteConstant.COMMENT_TABLE, dataList, new ArrayList<Map<String, String>>(), null, false);
        }
    }


    @Override
    public void onTouchContent(String commentID, String objectUser) {
        writeLayout.setVisibility(View.VISIBLE);
        //弹出软键盘
        commentContent.requestFocus();
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(commentContent ,InputMethodManager.SHOW_IMPLICIT);

        tempParentID = commentID;
        tempObjectUser = objectUser;
        isParent = false;
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
        getMenuInflater().inflate(R.menu.comment_menu, menu);
        return true;
    }
}
