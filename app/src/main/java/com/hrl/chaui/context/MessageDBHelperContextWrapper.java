package com.hrl.chaui.context;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;

import java.io.File;

// 该类重写getDataBasePath方法，来达到不同用户的message.db数据库文件分开存储的目的。
public class MessageDBHelperContextWrapper extends ContextWrapper {
    private Context context;

    public MessageDBHelperContextWrapper(Context base) {
        super(base);
        this.context = base;
    }

    @Override
    public File getDatabasePath(String name) {
        // 存储路径，data/data/<package name>/{{user_id}}/name

        SharedPreferences recv = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        int user_id = recv.getInt("user_id", -1);

        // 创建文件夹
        String dirPath = "data/data/" + context.getPackageName() + "/databases/" + user_id;
        File dirFile = new File(dirPath);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }

        // 获取具体数据库路径
        String dirAbsoPath = dirFile.getAbsolutePath();
        String path = null;
        if (dirAbsoPath.charAt(dirAbsoPath.length()-1) != '/') {
            path = dirAbsoPath + "/" + name;
        } else {
            path = dirAbsoPath + name;
        }

        File file = new File(path);

        return file;
    }
}
