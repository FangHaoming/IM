package com.hrl.chaui.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class MyDBHelper extends SQLiteOpenHelper {
    private Context mContext;

    public static final String CREATE_QQLOGIN="create table QQ_Login("
            +"id integer primary key autoincrement,"
            +"QQname text,"
            +"QQpwd text,"
            +"QQsign text,"
            +"QQgender text,"
            +"QQage text,"
            +"QQimg text)";
    public static final String INSERT_DATA="insert into QQ_Login(QQname,QQpwd,QQimg)values(?,?,?)";


    public MyDBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_QQLOGIN);
        //db.execSQL(INSERT_DATA,new String[]{"18hmfang","123456","123"});
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
