package com.bs.function;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bs.easy_chat.R;
import com.bs.parameter.Constant;
import com.bs.parameter.HandlerConstant;
import com.bs.tool_package.FastJSON;
import com.bs.tool_package.ImageTools;
import com.bs.util.BaseActivity;
import com.bs.util.MyListViewAdapter;
import com.bs.util.NetConnectionUtil;
import com.bs.widget.MyListView;

import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShoppingActivity extends BaseActivity{

    private final int NO_CONTENT = 2;

    List<Map<String,String>> shoppingList;
    Bitmap[] bitmaps;
    Handler myHandler;

    private MyListView listView;
    private Drawable drawable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopping_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");
        if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listView = (MyListView) findViewById(R.id.list_view);
        ((TextView)findViewById(R.id.title_name)).setText("购物");

        myHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                switch (msg.what)
                {
                    case HandlerConstant.SERVER_CONNECTION_ERROR:
                        listView.setVisibility(View.GONE);
                        ((ImageView)findViewById(R.id.warning)).setImageDrawable(ContextCompat.getDrawable(ShoppingActivity.this,R.drawable.disconnection));
                        break;

                    case NO_CONTENT:
                        listView.setVisibility(View.GONE);
                        ((ImageView)findViewById(R.id.warning)).setImageDrawable(ContextCompat.getDrawable(ShoppingActivity.this,R.drawable.no_content));
                        break;

                    case HandlerConstant.OPERATION_SUCCEED:
                        findViewById(R.id.warning).setVisibility(View.GONE);
                        initListView();
                }
            }
        };

        initData();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if(shoppingList != null)
            initListView();
    }

    //初始化列表数据
    private void initData()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {

                Message message = new Message();

                Map<String,String> map = new HashMap<>();

                map.put("msgType", Constant.QUERY_SHOPPING);

                String result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(),0);

                switch (result)
                {
                    case "[null]":
                        message.what = NO_CONTENT;
                        break;

                    case Constant.SERVER_CONNECTION_ERROR:
                        message.what = HandlerConstant.SERVER_CONNECTION_ERROR;
                        break;

                    default:message.what = HandlerConstant.OPERATION_SUCCEED;
                        shoppingList = FastJSON.parseJSON2ListString(result);
                        break;
                }

                //连接图片服务器下载图片
                if(message.what == HandlerConstant.OPERATION_SUCCEED)
                {
                    bitmaps = new Bitmap[shoppingList.size()];

                    for(int i=0,length = shoppingList.size();i<length;i++)
                    {
                        Map<String,String> pictureMap = new HashMap<>();
                        pictureMap.put("msgType",Constant.DOWNLOAD_PICTURE);
                        pictureMap.put("picPath",shoppingList.get(i).get("picture_path"));

                        byte[] pictureData = NetConnectionUtil.downLoadPicture(JSONObject.fromObject(pictureMap).toString());
                        if(pictureData!=null){
                            bitmaps[i] = ImageTools.createCircleImage(BitmapFactory.decodeByteArray(pictureData, 0, pictureData.length));
                        }
                        else
                            bitmaps[i] = ImageTools.createCircleImage(BitmapFactory.decodeResource(getResources(),R.drawable.no_picture));
                    }
                }

                myHandler.sendMessage(message);

            }
        }).start();

    }

    private void initListView()
    {
        MyListViewAdapter adapter = new MyListViewAdapter(shoppingList.size())
        {
            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {

                WebListViewHolder viewHolder;
                if (view == null) {
                    view = LayoutInflater.from(ShoppingActivity.this).inflate(R.layout.shopping_search_item, viewGroup, false);
                    viewHolder = new WebListViewHolder(view);
                    view.setTag(viewHolder);
                }
                else{
                    viewHolder = (WebListViewHolder) view.getTag();
                }

                if(i==shoppingList.size()-1)
                    view.findViewById(R.id.line).setVisibility(View.GONE);

                viewHolder.textView.setText(shoppingList.get(i).get("title"));

                drawable = new BitmapDrawable(ShoppingActivity.this.getResources(),bitmaps[i]);
                viewHolder.icon.setImageDrawable(drawable);

                return view;
            }
        };

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Bundle bundle = new Bundle();
                bundle.putString("link",shoppingList.get(i).get("link"));
                bundle.putString("title",shoppingList.get(i).get("title"));
                startActivity(new Intent(ShoppingActivity.this, WebActivity.class).putExtras(bundle));
            }
        });
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
