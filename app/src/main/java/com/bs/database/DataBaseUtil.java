package com.bs.database;

/**
 * Created by 13273 on 2017/4/19.
 *
 */

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.bs.parameter.SQLLiteConstant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataBaseUtil
{
    private static SQLiteDatabase sqLiteDatabase;

    public static void deleteUserData(){
        try{
            sqLiteDatabase=SQLiteDatabase.openDatabase
            (
                "/data/data/com.bs.easy_chat/databases",
                null,
                SQLiteDatabase.CREATE_IF_NECESSARY
            );

            String[] sql = new String[]
                    {"drop table if exists friend",
                    "drop table if exists words",
                    "drop table if exists secret",
                    "drop table if exists moments",
                    "drop table if exists favorite",
                    "drop table if exists comment",
                    "drop table if exists recommend",
                    "drop table if exists msg",
                    "drop table if exists chat",
                    "drop table if exists friend_request",
                    "drop table if exists protential_friend",
                    "drop table if exists callback"};
            for(int i=0 ;i<12;i++)
                sqLiteDatabase.execSQL(sql[i]);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            closeDatabase();
        }
    }

    //打开或者创建数据库
    public static synchronized void createOrOpenDataBase(int tableType)
    {
        try
        {
            sqLiteDatabase=SQLiteDatabase.openDatabase
                    (
                        "/data/data/com.bs.easy_chat/databases",
                        null,
                        SQLiteDatabase.CREATE_IF_NECESSARY
                    );

            String sql = "" ;

            switch (tableType)
            {
                case SQLLiteConstant.NOTES_TABLE:
                    sql=
                            "create table if not exists notes(" +
                                    "primary_key char(50) primary key,"+
                                    "date char(30)," +
                                    "detail_date char(30)," +
                                    "content text," +
                                    "user_id char(50)"+
                                    ")";
                    break;


                case SQLLiteConstant.DIARY_TABLE:
                    sql =
                            "create table if not exists diary("+
                                    "primary_key char(50) primary key,"+
                                    "date char(30),"+
                                    "detail_date char(30),"+
                                    "title text,"+
                                    "content text,"+
                                    "user_id char(50),"+
                                    "pictures text"+
                                    ")";
                    break;


                case SQLLiteConstant.FRIEND_TABLE:
                    sql =
                            "create table if not exists friend("+
                                    "friend_id varchar(50) primary key,"+
                                    "remark_name varchar(50),"+
                                    "shield_friend_moments char(2),"+
                                    "stealth_self_moments char(2)," +
                                    "sculpture varchar(50)," +
                                    "nickname varchar(50),"+
                                    "tel varchar(50),"+
                                    "gender varchar(4),"+
                                    "name varchar(50),"+
                                    "moments_background varchar(50),"+
                                    "self_introduction text,"+
                                    "instruction text,"+
                                    "handwriting varchar(100),"+
                                    "email varchar(50),"+
                                    "areas varchar(50)"+
                                    ")";
                    break;


                case SQLLiteConstant.WORDS_TABLE:
                    sql =
                            "create table if not exists words("+
                                    "words_id varchar(50) primary key,"+
                                    "user_id varchar(50),"+
                                    "write_to_user varchar(50),"+
                                    "content text," +
                                    "detail_time varchar(50)," +
                                    "nickname varchar(50)" +
                                    ")";
                    break;


                case SQLLiteConstant.SECRET_TABLE:
                    sql =
                            "create table if not exists secret("+
                                    "moments_id varchar(50) primary key,"+
                                    "detail_time varchar(50),"+
                                    "content text"+
                                    ")";
                    break;


                case SQLLiteConstant.MOMENTS_TABLE:
                    sql =
                            "create table if not exists moments("+
                                    "moments_id varchar(50) primary key,"+
                                    "user_id varchar(50),"+
                                    "detail_time varchar(50),"+
                                    "pictures_id text," +
                                    "content text,"+
                                    "favorite_number int"+
                                    ")";
                    break;

                case SQLLiteConstant.FAVORITE_TABLE:
                    sql =
                            "create table if not exists favorite("+
                                    "moments_id varchar(50) primary key)";
                    break;

                case SQLLiteConstant.RECOMMEND_TABLE:
                    sql =
                            "create table if not exists recommend("+
                                    "moments_id varchar(50) primary key,"+
                                    "user_id varchar(50),"+
                                    "detail_time varchar(50),"+
                                    "pictures_id text," +
                                    "content text,"+
                                    "favorite_number int"+
                                    ")";
                    break;


                case SQLLiteConstant.CALL_BACK:
                    sql =
                            "create table if not exists callback("+
                                    "comment_id varchar(50) primary key,"+
                                    "parent_id varchar(50),"+
                                    "moments_id varchar(50),"+
                                    "user_id varchar(50)," +
                                    "object_user varchar(50),"+
                                    "detail_time varchar(50),"+
                                    "content text,"+
                                    "author_id varchar(50)"+
                                    ")";
                    break;

                case SQLLiteConstant.COMMENT_TABLE:
                    sql =
                            "create table if not exists comment("+
                                    "comment_id varchar(50) primary key,"+
                                    "parent_id varchar(50),"+
                                    "moments_id varchar(50),"+
                                    "user_id varchar(50)," +
                                    "object_user varchar(50),"+
                                    "detail_time varchar(50),"+
                                    "content text,"+
                                    "author_id varchar(50)"+
                                    ")";
                    break;


                case SQLLiteConstant.MSG_TABLE:
                    sql =
                            "create table if not exists msg("+
                                    "comment_id varchar(50) primary key,"+
                                    "parent_id varchar(50),"+
                                    "moments_id varchar(50),"+
                                    "user_id varchar(50)," +
                                    "object_user varchar(50),"+
                                    "detail_time varchar(50),"+
                                    "content text,"+
                                    "author_id varchar(50)"+
                                    ")";
                    break;


                case SQLLiteConstant.AREA_TABLE:
                    sql =
                            "create table if not exists area("+
                                    "area_id varchar(50) primary key,"+
                                    "name varchar(50),"+
                                    "parent_id varchar(50)"+
                                    ")";
                    break;


                case SQLLiteConstant.CHAT_TABLE:
                    sql =
                            "create table if not exists chat("+
                                    "record_id varchar(50) primary key,"+
                                    "send_user varchar(50),"+
                                    "receive_user varchar(50),"+
                                    "time varchar(50),"+
                                    "type varchar(50),"+
                                    "content text,"+
                                    "is_send smallint,"+
                                    "state smallint"+
                                    ")";
                    break;

                case SQLLiteConstant.FRIEND_REQUEST:
                    sql =
                            "create table if not exists friend_request("+
                                    "record_id varchar(50) primary key,"+
                                    "user_id varchar(50),"+
                                    "request_user varchar(50),"+
                                    "reason varchar(100),"+
                                    "state varchar(10),"+
                                    "is_send smallint"+
                                    ")";
                    break;

                case SQLLiteConstant.PROTENTIAL_FRIEND:
                    sql =
                            "create table if not exists protential_friend("+
                                    "user_id varchar(50) primary key,"+
                                    "gender varchar(50),"+
                                    "sculpture varchar(50),"+
                                    "nickname varchar(50),"+
                                    "areas varchar(50),"+
                                    "self_introduction text,"+
                                    "handwriting varchar(100)"+
                                    ")";
                    break;
            }

            sqLiteDatabase.execSQL(sql);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    //关闭数据库
    public static void closeDatabase()
    {
        try{
            sqLiteDatabase.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }





    //数据库插入信息
    public static void insert(String insertSQLStr, int table)
    {
        createOrOpenDataBase(table);
        try{
            sqLiteDatabase.execSQL(insertSQLStr);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally {
            closeDatabase();
        }
    }

    //连续插入数据用此方法
    public static void insertContinuously(String insertSQLStr){
        try{
            sqLiteDatabase.execSQL(insertSQLStr);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    //删除记录的方法
    public static void delete(String deleteSQLStr, int table)
    {
        createOrOpenDataBase(table);
        try{
            sqLiteDatabase.execSQL(deleteSQLStr);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally {
            closeDatabase();
        }
    }

    //循环中连续删除记录用此方法
    public static void deleteContinuously(String deleteSQLStr){
        try{
            sqLiteDatabase.execSQL(deleteSQLStr);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    //修改记录的方法
    public static void updateContinuously(String updateSQLStr){
        try{
            sqLiteDatabase.execSQL(updateSQLStr);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    //修改记录的方法
    public static void update(String updateSQLStr ,int table)
    {
        createOrOpenDataBase(table);
        try{
            sqLiteDatabase.execSQL(updateSQLStr);
        }
        catch(Exception e){
            e.printStackTrace();
        }finally {
            closeDatabase();
        }
    }






    //查询的方法 返回结果集
    //查询方法每张表都有特定的方法
    public static List<Map<String,String>> queryNotes(String userID)
    {
        createOrOpenDataBase(SQLLiteConstant.NOTES_TABLE);

        List<Map<String,String>> list = new ArrayList<>();
        Cursor cursor=sqLiteDatabase.rawQuery("select primary_key,date,detail_date,content from notes where user_id = ? order by primary_key desc",new String[]{userID});
        try
        {
            while (cursor.moveToNext())
            {
                Map<String,String> map = new HashMap<>();
                for(int i=0;i<4;i++){
                    map.put(cursor.getColumnName(i),cursor.getString(i));
                }

                list.add(map);
            }
            cursor.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }finally {
            closeDatabase();
        }

        return list;
    }

    public static List<Map<String,String>> queryDiary(String userID)
    {
        createOrOpenDataBase(SQLLiteConstant.DIARY_TABLE);

        List<Map<String,String>> list = new ArrayList<>();
        Cursor cursor=sqLiteDatabase.rawQuery("select primary_key,date,detail_date,title,content,pictures from diary where user_id = ? order by primary_key desc",new String[]{userID});
        try{
            while (cursor.moveToNext())
            {
                Map<String,String> map = new HashMap<>();
                for(int i=0;i<6;i++){
                    map.put(cursor.getColumnName(i),cursor.getString(i));
                }

                list.add(map);
            }
            cursor.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }finally {
            closeDatabase();
        }

        return list;
    }





    public static List<Map<String,String>> queryFriends()
    {
        createOrOpenDataBase(SQLLiteConstant.FRIEND_TABLE);

        List<Map<String,String>> list = new ArrayList<>();
        Cursor cursor=sqLiteDatabase.rawQuery("select * from friend", new String[]{});
        try{
            while (cursor.moveToNext())
            {
                Map<String,String> map = new HashMap<>();
                for(int i=0;i<15;i++){
                    map.put(cursor.getColumnName(i),cursor.getString(i));
                }

                list.add(map);
            }
            cursor.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }finally {
            closeDatabase();
        }

        return list;
    }

    public static List<Map<String,String>> queryFriendsByCondition(String keyWords)
    {
        createOrOpenDataBase(SQLLiteConstant.FRIEND_TABLE);

        List<Map<String,String>> list = new ArrayList<>();
        Cursor cursor=sqLiteDatabase.rawQuery
                (
                    "select * from friend where friend_id like ? or nickname like ? or remark_name like ?",
                    new String[]{keyWords,keyWords,keyWords}
                );
        try{
            while (cursor.moveToNext())
            {
                Map<String,String> map = new HashMap<>();
                for(int i=0;i<15;i++){
                    map.put(cursor.getColumnName(i),cursor.getString(i));
                }

                list.add(map);
            }
            cursor.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }finally {
            closeDatabase();
        }

        return list;
    }

    public static List<Map<String,String>> queryAllFriendsID()
    {
        createOrOpenDataBase(SQLLiteConstant.FRIEND_TABLE);
        List<Map<String,String>> list = new ArrayList<>();
        Cursor cursor=sqLiteDatabase.rawQuery("select friend_id from friend",new String[]{});
        try{
            while (cursor.moveToNext())
            {
                Map<String,String> map = new HashMap<>();
                map.put(cursor.getColumnName(0),cursor.getString(0));
                list.add(map);
            }
            cursor.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }finally {
            closeDatabase();
        }
        return list;
    }

    public static List<Map<String,String>> queryFriendsExactly(String friendID)
    {
        createOrOpenDataBase(SQLLiteConstant.FRIEND_TABLE);

        List<Map<String,String>> list = new ArrayList<>();
        Cursor cursor=sqLiteDatabase.rawQuery
                (
                    "select * from friend where friend_id = ?",
                    new String[]{friendID}
                );
        try{
            while (cursor.moveToNext()){
                Map<String,String> map = new HashMap<>();
                for(int i=0;i<15;i++){
                    map.put(cursor.getColumnName(i),cursor.getString(i));
                }

                list.add(map);
            }
            cursor.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }finally {
            closeDatabase();
        }

        return list;
    }





    public static List<Map<String,String>> queryWordsProgressively(String userID, String range)
    {
        createOrOpenDataBase(SQLLiteConstant.WORDS_TABLE);

        List<Map<String,String>> list = new ArrayList<>();
        Cursor cursor;
        if(range.equals("now"))
            cursor=sqLiteDatabase.rawQuery
                    (
                        "select * from words where write_to_user = ? order by words_id desc",
                        new String[]{userID}
                    );
        else
            cursor=sqLiteDatabase.rawQuery
                    (
                        "select * from words where write_to_user = ? and words_id < ? order by words_id desc",
                        new String[]{userID,range}
                    );

        try{
            int count=0;
            while (cursor.moveToNext() && count<30){
                Map<String,String> map = new HashMap<>();
                for(int i=0;i<6;i++){
                    map.put(cursor.getColumnName(i),cursor.getString(i));
                }
                list.add(map);
                count++;
            }
            cursor.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }finally {
            closeDatabase();
        }

        return list;
    }

    public static List<Map<String,String>> queryWordsRefreshed(String userID, String range)
    {
        createOrOpenDataBase(SQLLiteConstant.WORDS_TABLE);

        List<Map<String,String>> list = new ArrayList<>();
        Cursor cursor=sqLiteDatabase.rawQuery
                (
                    "select * from words where write_to_user = ? and words_id >= ? order by words_id desc",
                    new String[]{userID,range}
                );
        try{
            while (cursor.moveToNext()){
                Map<String,String> map = new HashMap<>();
                for(int i=0;i<6;i++){
                    map.put(cursor.getColumnName(i),cursor.getString(i));
                }

                list.add(map);
            }
            cursor.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }finally {
            closeDatabase();
        }

        return list;
    }



    public static List<Map<String,String>> querySecretProgressively(String range)
    {
        createOrOpenDataBase(SQLLiteConstant.SECRET_TABLE);

        List<Map<String,String>> list = new ArrayList<>();
        Cursor cursor;
        if(range.equals("now"))
            cursor=sqLiteDatabase.rawQuery
                    (
                        "select * from secret order by moments_id desc",
                        new String[]{}
                    );
        else
            cursor=sqLiteDatabase.rawQuery
                    (
                        "select * from secret where moments_id < ? order by moments_id desc",
                        new String[]{range}
                    );

        try{
            int count=0;
            while (cursor.moveToNext() && count<15){
                Map<String,String> map = new HashMap<>();
                for(int i=0;i<3;i++){
                    map.put(cursor.getColumnName(i),cursor.getString(i));
                }
                list.add(map);
                count++;
            }
            cursor.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }finally {
            closeDatabase();
        }

        return list;
    }

    public static List<Map<String,String>> querySecretRefreshed(String range)
    {
        createOrOpenDataBase(SQLLiteConstant.SECRET_TABLE);

        List<Map<String,String>> list = new ArrayList<>();
        Cursor cursor=sqLiteDatabase.rawQuery
                (
                    "select * from secret where moments_id >= ? order by moments_id desc",
                    new String[]{range}
                );
        try{
            while (cursor.moveToNext()){
                Map<String,String> map = new HashMap<>();
                for(int i=0;i<3;i++){
                    map.put(cursor.getColumnName(i),cursor.getString(i));
                }

                list.add(map);
            }
            cursor.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }finally {
            closeDatabase();
        }

        return list;
    }



    public static List<Map<String,String>> queryMomentsProgressively(String range)
    {
        createOrOpenDataBase(SQLLiteConstant.MOMENTS_TABLE);

        List<Map<String,String>> list = new ArrayList<>();
        Cursor cursor;
        if(range.equals("now"))
            cursor=sqLiteDatabase.rawQuery
                    (
                        "select * from moments order by moments_id desc",
                        new String[]{}
                    );
        else
            cursor=sqLiteDatabase.rawQuery
                    (
                        "select * from moments where moments_id < ? order by moments_id desc",
                        new String[]{range}
                    );

        try{
            int count=0;
            while (cursor.moveToNext() && count<15){
                Map<String,String> map = new HashMap<>();
                for(int i=0;i<6;i++){
                    map.put(cursor.getColumnName(i),cursor.getString(i));
                }
                list.add(map);
                count++;
            }
            cursor.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }finally {
            closeDatabase();
        }

        return list;
    }

    public static List<Map<String,String>> queryMomentsRefreshed(String range)
    {
        createOrOpenDataBase(SQLLiteConstant.MOMENTS_TABLE);

        List<Map<String,String>> list = new ArrayList<>();
        Cursor cursor=sqLiteDatabase.rawQuery
                (
                    "select * from moments where moments_id >= ? order by moments_id desc",
                    new String[]{range}
                );
        try{
            while (cursor.moveToNext()){
                Map<String,String> map = new HashMap<>();
                for(int i=0;i<6;i++){
                    map.put(cursor.getColumnName(i),cursor.getString(i));
                }

                list.add(map);
            }
            cursor.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }finally {
            closeDatabase();
        }

        return list;
    }


    public static List<Map<String,String>> queryFavorite()
    {
        createOrOpenDataBase(SQLLiteConstant.FAVORITE_TABLE);

        List<Map<String,String>> list = new ArrayList<>();
        Cursor cursor=sqLiteDatabase.rawQuery
                (
                    "select * from favorite",
                    new String[]{}
                );
        try{
            while (cursor.moveToNext()){
                Map<String,String> map = new HashMap<>();
                for(int i=0;i<1;i++){
                    map.put(cursor.getColumnName(i),cursor.getString(i));
                }
                list.add(map);
            }
            cursor.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }finally {
            closeDatabase();
        }

        return list;
    }

    public static List<Map<String,String>> queryRecommend()
    {
        createOrOpenDataBase(SQLLiteConstant.RECOMMEND_TABLE);

        List<Map<String,String>> list = new ArrayList<>();
        Cursor cursor=sqLiteDatabase.rawQuery
                (
                        "select * from recommend order by favorite_number desc,moments_id desc",
                        new String[]{}
                );
        try{
            while (cursor.moveToNext()){
                Map<String,String> map = new HashMap<>();
                for(int i=0;i<6;i++){
                    map.put(cursor.getColumnName(i),cursor.getString(i));
                }
                list.add(map);
            }
            cursor.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }finally {
            closeDatabase();
        }

        return list;
    }


    public static List<Map<String,String>> queryComment(String momentsID)
    {
        createOrOpenDataBase(SQLLiteConstant.COMMENT_TABLE);

        List<Map<String,String>> list = new ArrayList<>();
        Cursor cursor=sqLiteDatabase.rawQuery
                (
                        "select * from comment where moments_id = ? order by comment_id asc",
                        new String[]{momentsID}
                );
        try{
            while (cursor.moveToNext()){
                Map<String,String> map = new HashMap<>();
                for(int i=0;i<8;i++){
                    map.put(cursor.getColumnName(i),cursor.getString(i));
                }
                list.add(map);
            }
            cursor.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }finally {
            closeDatabase();
        }

        return list;
    }

    public static List<Map<String,String>> queryCallBack(String objectUser)
    {
        createOrOpenDataBase(SQLLiteConstant.CALL_BACK);

        List<Map<String,String>> list = new ArrayList<>();
        Cursor cursor=sqLiteDatabase.rawQuery
                (
                        "select * from callback where object_user = ? order by comment_id desc",
                        new String[]{objectUser}
                );
        try{
            while (cursor.moveToNext()){
                Map<String,String> map = new HashMap<>();
                for(int i=0;i<8;i++){
                    map.put(cursor.getColumnName(i),cursor.getString(i));
                }
                list.add(map);
            }
            cursor.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }finally {
            closeDatabase();
        }

        return list;
    }

    public static List<Map<String,String>> queryMsg()
    {
        createOrOpenDataBase(SQLLiteConstant.MSG_TABLE);

        List<Map<String,String>> list = new ArrayList<>();
        Cursor cursor=sqLiteDatabase.rawQuery
                (
                        "select * from msg order by comment_id desc",
                        new String[]{}
                );
        try{
            while (cursor.moveToNext()){
                Map<String,String> map = new HashMap<>();
                for(int i=0;i<8;i++){
                    map.put(cursor.getColumnName(i),cursor.getString(i));
                }
                list.add(map);
            }
            cursor.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }finally {
            closeDatabase();
        }

        return list;
    }

    public static List<Map<String,String>> queryAlbumProgressively(String range, String userID)
    {
        createOrOpenDataBase(SQLLiteConstant.MOMENTS_TABLE);

        List<Map<String,String>> list = new ArrayList<>();
        Cursor cursor;
        if(range.equals("now"))
            cursor=sqLiteDatabase.rawQuery
                    (
                            "select * from moments where user_id = ? order by moments_id desc",
                            new String[]{userID}
                    );
        else
            cursor=sqLiteDatabase.rawQuery
                    (
                            "select * from moments where moments_id < ? and user_id = ? order by moments_id desc",
                            new String[]{range,userID}
                    );

        try{
            int count=0;
            while (cursor.moveToNext() && count<15){
                Map<String,String> map = new HashMap<>();
                for(int i=0;i<6;i++){
                    map.put(cursor.getColumnName(i),cursor.getString(i));
                }
                list.add(map);
                count++;
            }
            cursor.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }finally {
            closeDatabase();
        }

        return list;
    }

    public static List<Map<String,String>> queryAlbumRefreshed(String range, String userID)
    {
        createOrOpenDataBase(SQLLiteConstant.MOMENTS_TABLE);

        List<Map<String,String>> list = new ArrayList<>();
        Cursor cursor=sqLiteDatabase.rawQuery
                (
                        "select * from moments where moments_id >= ? and user_id = ? order by moments_id desc",
                        new String[]{range, userID}
                );
        try{
            while (cursor.moveToNext()){
                Map<String,String> map = new HashMap<>();
                for(int i=0;i<6;i++){
                    map.put(cursor.getColumnName(i),cursor.getString(i));
                }

                list.add(map);
            }
            cursor.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }finally {
            closeDatabase();
        }

        return list;
    }


    public static List<Map<String,String>> queryArea()
    {
        createOrOpenDataBase(SQLLiteConstant.AREA_TABLE);

        List<Map<String,String>> list = new ArrayList<>();
        Cursor cursor=sqLiteDatabase.rawQuery
                (
                    "select * from area",
                    new String[]{}
                );
        try{
            while (cursor.moveToNext()){
                Map<String,String> map = new HashMap<>();
                for(int i=0;i<3;i++){
                    map.put(cursor.getColumnName(i),cursor.getString(i));
                }

                list.add(map);
            }
            cursor.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }finally {
            closeDatabase();
        }
        return list;
    }


    public static List<Map<String,String>> queryMessage()
    {
        createOrOpenDataBase(SQLLiteConstant.CHAT_TABLE);

        List<Map<String,String>> list = new ArrayList<>();
        Cursor cursor=sqLiteDatabase.rawQuery
                (
                        "select * from chat order by record_id desc",
                        new String[]{}
                );
        try{
            while (cursor.moveToNext()){
                Map<String,String> map = new HashMap<>();
                for(int i=0;i<8;i++){
                    map.put(cursor.getColumnName(i),cursor.getString(i));
                }

                list.add(map);
            }
            cursor.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }finally {
            closeDatabase();
        }
        return list;
    }


    public static List<Map<String,String>> queryMessageWithFriend(String friendID){
        createOrOpenDataBase(SQLLiteConstant.CHAT_TABLE);

        List<Map<String,String>> list = new ArrayList<>();
        Cursor cursor=sqLiteDatabase.rawQuery
                (
                    "select * from chat where send_user = ? or receive_user = ? order by record_id asc",
                    new String[]{friendID,friendID}
                );
        try{
            while (cursor.moveToNext()){
                Map<String,String> map = new HashMap<>();
                for(int i=0;i<8;i++){
                    map.put(cursor.getColumnName(i),cursor.getString(i));
                }

                list.add(map);
            }
            cursor.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }finally {
            closeDatabase();
        }
        return list;
    }

    public static List<Map<String,String>> queryAddFriendRequest(){
        createOrOpenDataBase(SQLLiteConstant.FRIEND_REQUEST);

        List<Map<String,String>> list = new ArrayList<>();
        Cursor cursor=sqLiteDatabase.rawQuery
                (
                    "select * from friend_request order by record_id desc",
                    new String[]{}
                );
        try{
            while (cursor.moveToNext()){
                Map<String,String> map = new HashMap<>();
                for(int i=0;i<6;i++){
                    map.put(cursor.getColumnName(i),cursor.getString(i));
                }

                list.add(map);
            }
            cursor.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }finally {
            closeDatabase();
        }
        return list;
    }


    public static List<Map<String,String>> queryProtentialFriend(String userID){
        createOrOpenDataBase(SQLLiteConstant.PROTENTIAL_FRIEND);
        List<Map<String,String>> list = new ArrayList<>();
        Cursor cursor=sqLiteDatabase.rawQuery
                (
                        "select * from protential_friend where user_id = ?",
                        new String[]{userID}
                );
        try{
            while (cursor.moveToNext()){
                Map<String,String> map = new HashMap<>();
                for(int i=0;i<7;i++){
                    map.put(cursor.getColumnName(i),cursor.getString(i));
                }

                list.add(map);
            }
            cursor.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }finally {
            closeDatabase();
        }
        return list;
    }


}
