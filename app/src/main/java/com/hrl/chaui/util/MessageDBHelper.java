package com.hrl.chaui.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.hrl.chaui.context.MessageDBHelperContextWrapper;

public class MessageDBHelper extends SQLiteOpenHelper {

    public final static int CURVERSION = 2;

    private String messageTable = "create table Message (" +
            "uuid text primary key," +
            "msgId text default null," +
            "msgType integer not null, " +
            "senderId text  not null," +
            "targetId text  not null," +
            "sentTime integer default 0," +
            "sentStatus integer default null," +
            "checkStatus integer default 0," +
            "msgBody text not null, " +
            "isGroup integer default 0)";
    // checkStatus 是是否被查看的状态栏。

    public MessageDBHelper(@Nullable Context context, @Nullable String name, int version) {
        super(new MessageDBHelperContextWrapper(context), name, null, version);
        Log.e("MessageDBHelper", "当前数据库路径：" + (new MessageDBHelperContextWrapper(context).getDatabasePath(name)).getAbsolutePath());
    }

    public MessageDBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(new MessageDBHelperContextWrapper(context), name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(messageTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 1) { // 版本2：Message添加字段：判断是否为群聊。
            db.execSQL("alter table Message add column isGroup integer");
            ContentValues values = new ContentValues();
            values.put("isGroup", 0);
            int res = db.update("Message",values, null, null );
            Log.e("MessageDBHelper", "数据库更新成功，更新行数："+ res + "  数据库旧版本:" + oldVersion + " 新版本:" + newVersion);
        }
    }

}
