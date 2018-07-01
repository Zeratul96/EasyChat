package com.bs.person.person_center;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bs.database.DataBaseUtil;
import com.bs.easy_chat.R;
import com.bs.parameter.Constant;
import com.bs.parameter.Preference;
import com.bs.tool_package.ListViewHeight;
import com.bs.util.BaseActivity;
import com.bs.util.MainHandler;
import com.bs.util.MyListViewAdapter;
import com.bs.util.NetConnectionUtil;

import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AreaActivity extends BaseActivity implements View.OnClickListener{

    ArrayList<Map<String , String>> areaList;
    List<Map<String, String>> provinceList = new ArrayList<>();
    TextView cancelLayout;
    ListView listView;
    TextView finishBtn;
    private int selectedItem = -1;
    private int provinceItem = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.area_layout);

        cancelLayout = (TextView) findViewById(R.id.cancel_btn);
        cancelLayout.setOnClickListener(this);

        finishBtn = (TextView) findViewById(R.id.finish_btn);
        finishBtn.setOnClickListener(this);

        listView = (ListView) findViewById(R.id.list_view);
        areaList = (ArrayList<Map<String,String>>) DataBaseUtil.queryArea();
    }

    @Override
    protected void onStart() {
        super.onStart();
        selectedItem = -1;
        finishBtn.setEnabled(false);
        if(areaList != null)
            initListView();
    }


    private void initListView()
    {
        provinceList.clear();
        Iterator<Map<String ,String>> iterator = areaList.iterator();

        String provinceID="";
        String areaID = Preference.userInfoMap.get("areas");

        int c = -1;
        while (iterator.hasNext())
        {
            Map<String , String > map = iterator.next();

            //挑选出所选市隶属的省
            if(map.get("area_id").equals(areaID) && !map.get("parent_id").equals("0"))
            {
                provinceID = map.get("parent_id");
                break;
            }
        }

        iterator = areaList.iterator();

        while (iterator.hasNext())
        {
            Map<String, String> map = iterator.next();

            //筛选出省级
            if(map.get("parent_id").equals("0") && !map.get("parent_id").equals(map.get("area_id")))
            {
                provinceList.add(map);
                c++;
                if(map.get("area_id").equals(areaID))
                    selectedItem = c;

                if(map.get("area_id").equals(provinceID))
                    provinceItem = c;
            }
        }

        final MyListViewAdapter my = new MyListViewAdapter(provinceList.size()) {

            LayoutInflater inflater = LayoutInflater.from(AreaActivity.this);

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                LinearLayout content = (LinearLayout) view;
                if(content==null){

                    if(provinceList.get(i).get("area_id").startsWith("1"))
                        content = (inflater.inflate(R.layout.city_item, viewGroup, false)).findViewById(R.id.content_layout);

                    else
                        content = (inflater.inflate(R.layout.area_item, viewGroup, false)).findViewById(R.id.content_layout);
                }

                TextView tx = content.findViewById(R.id.area_name);
                tx.setText(provinceList.get(i).get("name"));

                if(i == provinceList.size()-1)
                    content.findViewById(R.id.line).setVisibility(View.GONE);

                //特别行政区打钩
                if(selectedItem == i)
                    ((ImageView)content.findViewById(R.id.arrow)).setImageDrawable(ContextCompat.getDrawable(AreaActivity.this,R.drawable.blue_circle));

                //省份做标注
                if(provinceItem == i)
                    ((TextView)content.findViewById(R.id.mark)).setText("已选地区");

                return content;
            }
        };

        listView.setAdapter(my);
        ListViewHeight.setListViewHeight(listView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if(!provinceList.get(i).get("area_id").startsWith("1"))
                {
                    Bundle bundle = new Bundle();
                    bundle.putString("province",provinceList.get(i).get("area_id"));
                    startActivity(new Intent(AreaActivity.this, CityActivity.class).putExtras(bundle).putExtra("msg",areaList));
                }

                else
                {
                    finishBtn.setEnabled(true);

                    if(selectedItem != -1 && selectedItem-listView.getFirstVisiblePosition()>=0)
                    {
                        LinearLayout per = (LinearLayout) listView.getChildAt(selectedItem - listView.getFirstVisiblePosition());
                        ((ImageView)per.findViewById(R.id.arrow)).setImageDrawable(null);
                    }

                    selectedItem = i;
                    LinearLayout per = (LinearLayout) listView.getChildAt(i - listView.getFirstVisiblePosition());
                    per.setBackgroundColor(Color.parseColor("#FFFFFF"));

                    if(i > 0 && i-1-listView.getFirstVisiblePosition() >= 0)
                    {
                        LinearLayout lastPer = (LinearLayout) listView.getChildAt(i - 1 - listView.getFirstVisiblePosition());
                        lastPer.getChildAt(1).setBackgroundColor(ContextCompat.getColor(AreaActivity.this, R.color.line_gray));
                    }

                    ((ImageView)per.findViewById(R.id.arrow)).setImageDrawable(ContextCompat.getDrawable(AreaActivity.this,R.drawable.blue_circle));
                }
            }
        });
    }

    @Override
    public void onClick(View view) {

        if(view == cancelLayout) finish();

        else{
            final Map<String , String > map = new HashMap<>();
            map.put("msgType", Constant.UPDATE_USER);
            map.put("mode", "3");
            map.put("userID", Preference.userInfoMap.get("user_id"));
            map.put("areas",provinceList .get(selectedItem).get("area_id"));

            new Thread(new Runnable() {
                @Override
                public void run() {
                    final String result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(),0);
                    MainHandler.getInstance().post(new Runnable() {
                        @Override
                        public void run() {
                            if(!result.equals(Constant.SERVER_CONNECTION_ERROR) && !result.equals("[null]")){
                                Preference.userInfoMap.put("areas",provinceList.get(selectedItem).get("area_id"));
                                SharedPreferences.Editor editor = getSharedPreferences("userInfo", MODE_PRIVATE).edit();
                                editor.putString("userInfo", JSONObject.fromObject(Preference.userInfoMap).toString());
                                editor.apply();
                                finish();
                            }

                            else Toast.makeText(AreaActivity.this, "网络连接异常，请检查网络连接。", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).start();
        }
    }
}
