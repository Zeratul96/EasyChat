package com.bs.function.secret;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bs.easy_chat.R;
import com.bs.tool_package.TimeTools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 13273 on 2017/10/28.
 *
 */

class SecretAdapter extends RecyclerView.Adapter<SecretAdapter.ViewHolder>{

    private List<Map<String,String>> detailData;
    private Context context;

    SecretAdapter(Context context, List<Map<String,String>> detailData){
        this.context = context;
        this.detailData = detailData;
    }

    public SecretAdapter setData(List<Map<String,String>> detailData){
        this.detailData = detailData;
        return this;
    }

    @Override
    public int getItemCount() {return detailData == null?0:detailData.size();}

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.secret_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        HashMap<String,String> map = (HashMap<String, String>) detailData.get(position);
        holder.textContent.setText(map.get("content"));
        holder.time.setText(TimeTools.parseDetailTime(map.get("detail_time")));
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView textContent;
        TextView time;

        ViewHolder(View view){
            super(view);
            textContent = view.findViewById(R.id.content);
            time = view.findViewById(R.id.time);
        }
    }
}
