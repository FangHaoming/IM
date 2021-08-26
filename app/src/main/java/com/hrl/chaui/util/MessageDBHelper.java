package com.hrl.chaui.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class MessageDBHelper extends SQLiteOpenHelper {

    public final static int VERSION = 1;

    private String messageTable = "create table Message (" +
            "uuid text primary key," +
            "msgId text default null," +
            "msgType integer not null, " +
            "senderId text  not null," +
            "targetId text  not null," +
            "sentTime integer default 0," +
            "sentStatus integer default null," +
            "checkStatus integer default 0," +
            "msgBody text not null)";
    // checkStatus 是是否被查看的状态栏。

    public MessageDBHelper(@Nullable Context context, @Nullable String name, int version) {
        super(context, name, null, version);
    }

    public MessageDBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(messageTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
