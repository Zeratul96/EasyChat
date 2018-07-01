package com.bs.function.words;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bs.database.DataBaseUtil;
import com.bs.easy_chat.R;
import com.bs.parameter.Constant;
import com.bs.parameter.Preference;
import com.bs.tool_package.TimeTools;
import com.bs.util.MainHandler;
import com.bs.util.NetConnectionUtil;

import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 13273 on 2017/10/28.
 *
 */

class WordsAdapter extends RecyclerView.Adapter<WordsAdapter.ViewHolder> {

    private Context context;
    private List<Map<String,String>> detailData;
    private String boardOwner;
    private boolean selfBoard;
    private List<Map<String,String>> userInfoList;
    private AlertDialog alertDialog;

    WordsAdapter(Context context, List<Map<String,String>> detailData, String boardOwner){
        this.context = context;
        this.detailData = detailData;
        this.boardOwner = boardOwner;
        selfBoard = boardOwner.equals(Preference.userInfoMap.get("user_id"));
        userInfoList = DataBaseUtil.queryFriends();
    }

    public WordsAdapter setData(List<Map<String,String>> detailData){
        this.detailData = detailData;
        userInfoList = DataBaseUtil.queryFriends();
        return this;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? 0:1;
    }

    @Override
    public int getItemCount(){return  detailData == null?1:detailData.size()+1;}

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return viewType == 0 ?
                new ViewHolder(LayoutInflater.from(context).inflate(R.layout.words_fill_item,parent,false)):
                new ViewHolder(LayoutInflater.from(context).inflate(R.layout.words_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        if (position == 0) {
            holder.mainContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString("write_to_user", boardOwner);
                    context.startActivity(new Intent(context, WriteWordsActivity.class).putExtras(bundle));
                }
            });
        }
        //具体内容
        else
        {
            final HashMap<String, String> piece = (HashMap<String, String>) detailData.get(position - 1);
            holder.deletion.setVisibility(selfBoard ? View.VISIBLE : View.GONE);
            holder.deletion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                Map<String, String> map = new HashMap<>();
                map.put("msgType", Constant.DELETE_WORDS);
                map.put("words_id", piece.get("words_id"));
                showAlertDialog(map, position);
                }
            });
            holder.content.setText(piece.get("content"));
            holder.time.setText(TimeTools.parseDetailTime(piece.get("detail_time")));

            String name = piece.get("nickname");
            //不是自己
            if (!name.equals(Preference.userInfoMap.get("user_id"))) {
                Map<String, String> userInfo = findUserInfo(piece.get("user_id"));
                if (userInfo != null && userInfo.get("remark_name").length() > 0) {
                    name = userInfo.get("remark_name");
                }
            }
            holder.author.setText("by  " + name);
        }
    }


    private Map<String,String> findUserInfo(@NonNull String userID){

        for (Map<String,String> map : userInfoList){
            if(map.get("friend_id").equals(userID)){
                return map;
            }
        }
        return null;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        View mainContent;
        TextView content;
        TextView time;
        TextView author;
        TextView deletion;
        ViewHolder(View view){
            super(view);
            mainContent = view.findViewById(R.id.main_content);
            content = view.findViewById(R.id.content);
            time = view.findViewById(R.id.time);
            author = view.findViewById(R.id.author);
            deletion = view.findViewById(R.id.deletion);
        }
    }

    private void showAlertDialog(final Map<String,String> map, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogCustom);
        builder.setTitle("提示");
        //对话框内容
        builder.setMessage("确定要删除此条留言吗？");
        //确定按钮
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final String result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(), 0);
                        MainHandler.getInstance().post(new Runnable() {
                            @Override
                            public void run() {
                                if (result.equals(Constant.SERVER_CONNECTION_ERROR))
                                    Toast.makeText(context, context.getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                                else
                                    detailData.remove(position - 1);
                                WordsAdapter.this.notifyDataSetChanged();
                            }
                        });
                    }
                }).start();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });

        //固定对话框使其不可被取消
        builder.setCancelable(false);
        //创建对话框
        alertDialog = builder.create();
        alertDialog.show();
    }
}
