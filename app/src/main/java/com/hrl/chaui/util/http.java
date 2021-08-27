package com.hrl.chaui.util;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hrl.chaui.bean.User;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.hrl.chaui.MyApplication.contactData;
import static com.hrl.chaui.MyApplication.groupData;

public class http {
    public static void sendByPost(Context mContext, Integer user_id) {
        JSONObject json = new JSONObject();
        json.put("user_id", user_id);
        String path = "http://40f730q296.qicp.vip/userContacts";
        OkHttpClient client = new OkHttpClient();
        final FormBody formBody = new FormBody.Builder()
                .add("json", json.toJSONString())
                .build();
        Request request = new Request.Builder()
                .url(path)
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Looper.prepare();
                Toast.makeText(mContext, "服务器连接失败", Toast.LENGTH_SHORT).show();
                Looper.loop();
                e.printStackTrace();
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                String info = response.body().string();
                JSONObject json = JSON.parseObject(info);
                JSONArray contacts = json.getJSONArray("contacts");
                System.out.println("*********contact" + json.toJSONString()+mContext);
                contactData = new ArrayList<>();
                groupData = new ArrayList<>();
                contactData.add((User) new User("新的朋友").setTop(true).setBaseIndexTag("↑"));
                contactData.add((User) new User("群聊").setTop(true).setBaseIndexTag("↑"));
                for (int i = 0; i < contacts.size(); i++) {
                    JSONObject obj = contacts.getJSONObject(i);
                    if (obj.getInteger("type") == 0) {
                        contactData.add(new User(obj.getInteger("contact_id"), obj.getString("contact_img"), obj.getString("contact_name"), obj.getInteger("type")));
                    } else {
                        groupData.add(new User(obj.getInteger("contact_id"), obj.getString("contact_img"), obj.getString("contact_name"), obj.getInteger("type")));

                    }
                }

            }
        });
    }
}
