package com.bs.person.person_center;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bs.easy_chat.R;
import com.bs.parameter.Constant;
import com.bs.parameter.Preference;
import com.bs.tool_package.ListViewHeight;
import com.bs.util.ActivityListUtil;
import com.bs.util.BaseActivity;
import com.bs.util.MainHandler;
import com.bs.util.MyListViewAdapter;
import com.bs.util.NetConnectionUtil;
import com.bs.widget.MyListView;

import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CityActivity extends BaseActivity implements View.OnClickListener{

    ArrayList<Map<String,String>> areaList;
    MyListView listView;
    List<Map<String, String>> cityList = new ArrayList<>();
    TextView finishBtn;
    private int selectedItem = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.city_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");
        if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        finishBtn = (TextView) findViewById(R.id.finish_btn);
        finishBtn.setOnClickListener(this);

        listView = (MyListView) findViewById(R.id.list_view);
        areaList = (ArrayList<Map<String,String>>) getIntent().getSerializableExtra("msg");
    }

    @Override
    protected void onStart() {
        super.onStart();
        initListView();
    }

    @Override
    public void onClick(View view) {
        final Map<String , String > map = new HashMap<>();
        map.put("msgType", Constant.UPDATE_USER);
        map.put("mode", "3");
        map.put("userID", Preference.userInfoMap.get("user_id"));
        map.put("areas", cityList.get(selectedItem).get("area_id"));

        new Thread(new Runnable() {
            @Override
            public void run() {
                final String result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(),0);

                MainHandler.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        switch (result)
                        {
                            case "[null]":

                            case Constant.SERVER_CONNECTION_ERROR:
                                Toast.makeText(CityActivity.this, "网络连接异常，请检查网络连接。", Toast.LENGTH_SHORT).show();
                                break;

                            default:
                                Preference.userInfoMap.put("areas",cityList.get(selectedItem).get("area_id"));
                                SharedPreferences.Editor editor = getSharedPreferences("userInfo", MODE_PRIVATE).edit();
                                editor.putString("userInfo", JSONObject.fromObject(Preference.userInfoMap).toString());
                                editor.apply();
                                finish();
                                ActivityListUtil.destroyLastActivity(2);
                                break;
                        }
                    }
                });
            }
        }).start();
    }

    private void initListView()
    {
        cityList.clear();
        Iterator<Map<String ,String>> iterator = areaList.iterator();
        String areaID = Preference.userInfoMap.get("areas");

        int c = -1;
        while (iterator.hasNext())
        {
            Map<String, String> map = iterator.next();
            if(map.get("parent_id").equals(getIntent().getExtras().get("province")))
            {
                cityList.add(map);
                c++;
                if(map.get("area_id").equals(areaID))
                    selectedItem = c;
            }
        }

        final MyListViewAdapter my = new MyListViewAdapter(cityList.size()) {
            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                LinearLayout content = (LinearLayout) view;
                if(content==null){
                    content = (LayoutInflater.from(CityActivity.this).inflate(R.layout.city_item, viewGroup, false)).findViewById(R.id.content_layout);
                }

                TextView tx = content.findViewById(R.id.area_name);
                tx.setText(cityList.get(i).get("name"));

                if(i == cityList.size()-1)
                    content.findViewById(R.id.line).setVisibility(View.GONE);

                if(i == selectedItem)
                    ((ImageView)content.findViewById(R.id.arrow)).setImageDrawable(ContextCompat.getDrawable(CityActivity.this, R.drawable.blue_circle));

                return content;
            }
        };

        listView.setAdapter(my);
        ListViewHeight.setListViewHeight(listView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                finishBtn.setEnabled(true);

                if(selectedItem != -1)
                {
                    if(selectedItem-listView.getFirstVisiblePosition()>=0)
                    {
                        LinearLayout per = (LinearLayout) listView.getChildAt(selectedItem - listView.getFirstVisiblePosition());
                        ((ImageView)per.findViewById(R.id.arrow)).setImageDrawable(null);
                    }

                    else
                        my.notifyDataSetChanged();
                }

                selectedItem = i;

                LinearLayout per = (LinearLayout) listView.getChildAt(i - listView.getFirstVisiblePosition());
                per.setBackgroundColor(Color.parseColor("#FFFFFF"));

                if(i > 0)
                {
                    if(i - 1 - listView.getFirstVisiblePosition()>=0)
                    {
                        LinearLayout lastPer = (LinearLayout) listView.getChildAt(i - 1 - listView.getFirstVisiblePosition());
                        lastPer.getChildAt(1).setBackgroundColor(ContextCompat.getColor(CityActivity.this, R.color.line_gray));
                    }

                    else
                        my.notifyDataSetChanged();
                }

                ((ImageView)per.findViewById(R.id.arrow)).setImageDrawable(ContextCompat.getDrawable(CityActivity.this,R.drawable.blue_circle));
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
