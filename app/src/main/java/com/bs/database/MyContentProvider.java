package com.bs.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by 13273 on 2017/4/20.
 *
 */

public class MyContentProvider extends ContentProvider
{
    private static final UriMatcher matcher;

    static
    {
        matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI("easy_chat.provider.data", "data",1);
    }

    SQLiteDatabase sqLiteDatabase;

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder)
    {
        switch (matcher.match(uri))
        {
            case 1:
                Cursor cur=sqLiteDatabase.query
                        (
                            "data",
                            projection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sortOrder
                        );
            return cur;
        }

        return null;
    }

    @Override
    public boolean onCreate()
    {
        sqLiteDatabase=SQLiteDatabase.openDatabase
                (
                        "/data/data/com.bs.easy_chat/databases", //数据库所在路径
                        null, 								//CursorFactory
                        SQLiteDatabase.CREATE_IF_NECESSARY //读写、若不存在则创建
                );

        return false;
    }

    @Override
    public String getType(Uri uri) {return null;}

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {return 0;}

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {return null;}

    @Override
    public int delete(Uri uri, String s, String[] strings) {return 0;}
}
