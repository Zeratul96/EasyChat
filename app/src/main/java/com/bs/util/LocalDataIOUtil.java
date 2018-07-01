package com.bs.util;


import android.support.annotation.NonNull;
import android.util.Log;

import com.bs.database.DataBaseUtil;
import com.bs.parameter.SQLLiteConstant;
import com.bs.tool_package.ListCompareTools;

import java.util.List;
import java.util.Map;

/**
 * Created by 13273 on 2017/11/6.
 * 本类为各类分布式数据加载本地缓存的核心代码
 * 涉及使用到本类的模块有：那一刻、留言板、悄悄话、评论、今日推荐、好友信息、点赞表、地址
 */

public class LocalDataIOUtil {

    public static void syncLocalData(int table, @NonNull List<Map<String,String>> localData, @NonNull List<Map<String,String>> serverData, List<Map<String,String>> continueData, boolean isContinueLoadData){
        //找出localData不在serverData中的元素（直接删除）
        List<Map<String,String>> deleteList = ListCompareTools.listFilter(isContinueLoadData?continueData:localData, serverData);
        Log.d("deleteSize", deleteList.size()+"");
        switch (table){
            case SQLLiteConstant.WORDS_TABLE:
                DataBaseUtil.createOrOpenDataBase(SQLLiteConstant.WORDS_TABLE);
                for(Map<String,String> map:deleteList)
                    DataBaseUtil.deleteContinuously("delete from words where words_id = '"+map.get("words_id")+"'");
                break;

            case SQLLiteConstant.SECRET_TABLE:
                DataBaseUtil.createOrOpenDataBase(SQLLiteConstant.SECRET_TABLE);
                for(Map<String,String> map:deleteList)
                    DataBaseUtil.deleteContinuously("delete from secret where moments_id = '"+map.get("moments_id")+"'");
                break;

            case SQLLiteConstant.MOMENTS_TABLE:
                DataBaseUtil.createOrOpenDataBase(SQLLiteConstant.MOMENTS_TABLE);
                for(Map<String, String> map: deleteList)
                    DataBaseUtil.deleteContinuously("delete from moments where moments_id = '"+map.get("moments_id")+"'");
                break;

            case SQLLiteConstant.FAVORITE_TABLE:
                DataBaseUtil.createOrOpenDataBase(SQLLiteConstant.FAVORITE_TABLE);
                for(Map<String,String> map: deleteList)
                    DataBaseUtil.deleteContinuously("delete from favorite where moments_id = '"+map.get("moments_id")+"'");
                break;

            case SQLLiteConstant.RECOMMEND_TABLE:
                DataBaseUtil.createOrOpenDataBase(SQLLiteConstant.RECOMMEND_TABLE);
                for(Map<String,String> map: deleteList)
                    DataBaseUtil.deleteContinuously("delete from recommend where moments_id = '"+map.get("moments_id")+"'");
                break;

            case SQLLiteConstant.COMMENT_TABLE:
                DataBaseUtil.createOrOpenDataBase(SQLLiteConstant.COMMENT_TABLE);
                for(Map<String,String> map: deleteList)
                    DataBaseUtil.deleteContinuously("delete from comment where comment_id = '"+map.get("comment_id")+"'");
                break;

            case SQLLiteConstant.CALL_BACK:
                DataBaseUtil.createOrOpenDataBase(SQLLiteConstant.CALL_BACK);
                for(Map<String,String> map: deleteList)
                    DataBaseUtil.deleteContinuously("delete from callback where comment_id = '"+map.get("comment_id")+"'");
                break;

            case SQLLiteConstant.MSG_TABLE:
                DataBaseUtil.createOrOpenDataBase(SQLLiteConstant.MSG_TABLE);
                for(Map<String,String> map: deleteList)
                    DataBaseUtil.deleteContinuously("delete from msg where comment_id = '"+map.get("comment_id")+"'");
                break;

            case SQLLiteConstant.FRIEND_TABLE:
                DataBaseUtil.createOrOpenDataBase(SQLLiteConstant.FRIEND_TABLE);
                for(Map<String,String> map: deleteList)
                {
                    DataBaseUtil.deleteContinuously("delete from friend where friend_id = '"+map.get("friend_id")+"'");
                    DataBaseUtil.deleteContinuously("delete from chat where send_user = '"+map.get("friend_id")+"' or receive_user = '"+map.get("friend_id")+"'");
                    DataBaseUtil.updateContinuously("update friend_request set state = 'delete' where user_id = '"+map.get("friend_id")+"' or request_user = '"+map.get("friend_id")+"'");
                }
                break;

            case SQLLiteConstant.AREA_TABLE:
                DataBaseUtil.createOrOpenDataBase(SQLLiteConstant.AREA_TABLE);
                for(Map<String,String> map: deleteList)
                    DataBaseUtil.deleteContinuously("delete from area where area_id = '"+map.get("area_id")+"'");
                break;
        }
        //找出serverData不在localData中的元素（添加）
        List<Map<String,String>> insertList = ListCompareTools.listFilter(serverData, isContinueLoadData?continueData:localData);
        Log.d("insertSize", insertList.size()+"");
        switch (table){
            case SQLLiteConstant.WORDS_TABLE:
                for(Map<String,String> map:insertList){
                    String sql = "insert into words values('"+map.get("words_id")+"','"+map.get("user_id")+"','"+map.get("write_to_user")+"','"+map.get("content")+"','"+map.get("detail_time")+"','"+map.get("nickname")+"')";
                    DataBaseUtil.insertContinuously(sql);
                }
                break;

            case SQLLiteConstant.SECRET_TABLE:
                for(Map<String,String> map:insertList){
                    String sql = "insert into secret values('"+map.get("moments_id")+"','"+map.get("detail_time")+"','"+map.get("content")+"')";
                    DataBaseUtil.insertContinuously(sql);
                }
                break;

            case SQLLiteConstant.MOMENTS_TABLE:
                for(Map<String,String> map:insertList){
                    String sql = "insert into moments values('"+map.get("moments_id")+"','"+map.get("user_id")+"','"+map.get("detail_time")+"','"+map.get("pictures_id")+"','"+map.get("content")+"','"+map.get("favorite_number")+"')";
                    DataBaseUtil.insertContinuously(sql);
                }
                break;

            case SQLLiteConstant.FAVORITE_TABLE:
                for(Map<String,String> map:insertList){
                    String sql = "insert into favorite values('"+map.get("moments_id")+"')";
                    DataBaseUtil.insertContinuously(sql);
                }
                break;

            case SQLLiteConstant.RECOMMEND_TABLE:
                for(Map<String,String> map:insertList){
                    String sql = "insert into recommend values('"+map.get("moments_id")+"','"+map.get("user_id")+"','"+map.get("detail_time")+"','"+map.get("pictures_id")+"','"+map.get("content")+"','"+map.get("favorite_number")+"')";
                    DataBaseUtil.insertContinuously(sql);
                }
                break;

            case SQLLiteConstant.COMMENT_TABLE:
                for(Map<String,String> map: insertList){
                    String sql = "insert into comment values('"+map.get("comment_id")+"','"+map.get("parent_id")+"','"+map.get("moments_id")+"','"+map.get("user_id")+"','"+map.get("object_user")+"','"+map.get("detail_time")+"','"+map.get("content")+"','"+map.get("author_id")+"')";
                    DataBaseUtil.insertContinuously(sql);
                }
                break;

            case SQLLiteConstant.CALL_BACK:
                for(Map<String,String> map: insertList){
                    String sql = "insert into callback values('"+map.get("comment_id")+"','"+map.get("parent_id")+"','"+map.get("moments_id")+"','"+map.get("user_id")+"','"+map.get("object_user")+"','"+map.get("detail_time")+"','"+map.get("content")+"','"+map.get("author_id")+"')";
                    DataBaseUtil.insertContinuously(sql);
                }
                break;

            case SQLLiteConstant.MSG_TABLE:
                for(Map<String,String> map: insertList){
                    String sql = "insert into msg values('"+map.get("comment_id")+"','"+map.get("parent_id")+"','"+map.get("moments_id")+"','"+map.get("user_id")+"','"+map.get("object_user")+"','"+map.get("detail_time")+"','"+map.get("content")+"','"+map.get("author_id")+"')";
                    DataBaseUtil.insertContinuously(sql);
                }
                break;

            case SQLLiteConstant.FRIEND_TABLE:
                for(Map<String,String> map: insertList){
                    String sql = "insert into friend values('"+map.get("friend_id")+"','"+map.get("remark_name")+"','"+map.get("shield_friend_moments")+"','"+map.get("stealth_self_moments")+"','"+map.get("sculpture")+"','"+map.get("nickname")+"','"+map.get("tel")+"','"+map.get("gender")+"','"
                            +map.get("name")+"','"+map.get("moments_background")+"','"+map.get("self_introduction")+"','"+map.get("instruction")+"','"+map.get("handwriting")+"','"+map.get("email")+"','"+map.get("areas")+"')";
                    DataBaseUtil.insertContinuously(sql);
                }
                break;

            case SQLLiteConstant.AREA_TABLE:
                for(Map<String,String> map: insertList){
                    String sql = "insert into area values('"+map.get("area_id")+"','"+map.get("name")+"','"+map.get("parent_id")+"')";
                    DataBaseUtil.insertContinuously(sql);
                }
                break;
        }
        DataBaseUtil.closeDatabase();

        //把localData中所有元素全部替换成serverData中元素（避免改变对象引用）
        // 这个模式只有refresh和第一次query可以用，而补充加载数据把服务器数据继续塞入List即可
        if(!isContinueLoadData) localData.clear();
        for(Map<String,String> map:serverData)
            localData.add(map);
    }

    /**
     *
     * @param table:表格
     * @param localData:本地数据
     * @param serverData:服务器数据
     * @return 数据是否不同
     */
    public static boolean syncSingleData(int table, @NonNull Map<String,String> localData, @NonNull Map<String,String> serverData){
        if(!localData.equals(serverData))
        {
            switch (table){
                case SQLLiteConstant.FRIEND_TABLE:
                    DataBaseUtil.createOrOpenDataBase(SQLLiteConstant.FRIEND_TABLE);
                    DataBaseUtil.deleteContinuously("delete from friend where friend_id = '"+localData.get("friend_id")+"'");
                    break;
            }

            switch (table){
                case SQLLiteConstant.FRIEND_TABLE:
                    String sql = "insert into friend values('"+serverData.get("friend_id")+"','"+serverData.get("remark_name")+"','"+serverData.get("shield_friend_moments")+"','"+serverData.get("stealth_self_moments")+"','"+serverData.get("sculpture")+"','"+serverData.get("nickname")+"','"+serverData.get("tel")+"','"+serverData.get("gender")+"','"
                            +serverData.get("name")+"','"+serverData.get("moments_background")+"','"+serverData.get("self_introduction")+"','"+serverData.get("instruction")+"','"+serverData.get("handwriting")+"','"+serverData.get("email")+"','"+serverData.get("areas")+"')";
                    DataBaseUtil.insertContinuously(sql);
                    break;
            }

            DataBaseUtil.closeDatabase();
            return true;
        }

        else return false;
    }
}
