package com.hrl.chaui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hrl.chaui.R;
import com.hrl.chaui.adapter.GridViewAdapter;
import com.hrl.chaui.adapter.GroupMemberAdapter;
import com.hrl.chaui.bean.User;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.hrl.chaui.MyApplication.groupMemberData;

public class GroupInfoActivity extends AppCompatActivity {
    public SharedPreferences recv;
    public SharedPreferences.Editor editor;
    public TextView back_arrow;
    public RecyclerView mRv;
    public View notice_switch;
    public TextView name;
    public TextView delete;
    public GridView gridView;
    public GroupMemberAdapter mAdapter;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_group_info);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.top_bottom));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        back_arrow=findViewById(R.id.back_arrow);
        back_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(GroupInfoActivity.this, GroupActivity.class);
                startActivity(intent);
                overridePendingTransition(0, R.anim.slide_right_out);
            }
        });

        /*
        mRv=findViewById(R.id.rv);
        mRv.setLayoutManager(new LinearLayoutManager(this));

         */
        name=findViewById(R.id.name);
        notice_switch=findViewById(R.id.notice_switch);
        gridView=findViewById(R.id.gridview);

        recv = getSharedPreferences("data", Context.MODE_PRIVATE);
        editor = recv.edit();
        Intent intent=getIntent();
        Bundle bundle = intent.getExtras();
        int group_id=bundle.getInt("contact_id");

        sendByPost(group_id,recv.getInt("user_id",0));
    }

    private void sendByPost(int group_id,int user_id) {
        JSONObject json=new JSONObject();
        json.put("group_id",group_id);
        json.put("user_id",user_id);
        String path = getResources().getString(R.string.request_local)+"/groupSearch";
        OkHttpClient client = new OkHttpClient();
        final FormBody formBody = new FormBody.Builder()
                .add("json", json.toJSONString())
                .build();
        System.out.println("*********"+json.toJSONString());
        Request request = new Request.Builder()
                .url(path)
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Looper.prepare();
                Toast.makeText(GroupInfoActivity.this, "服务器连接失败", Toast.LENGTH_SHORT).show();
                Looper.loop();
                e.printStackTrace();
            }
            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                String info=response.body().string();
                System.out.println("***********group_info"+info);
                JSONObject json= JSON.parseObject(info);
                JSONArray members=json.getJSONArray("members");
                groupMemberData=new ArrayList<>();
                for(int i=0;i<members.size();i++){
                    JSONObject obj= members.getJSONObject(i);
                    User user=new User();
                    user.setImg(obj.getString("user_img"));
                    user.setName(obj.getString("user_name"));
                    user.setId(obj.getInteger("user_id"));
                    user.setPhone(obj.getString("user_phone"));
                    user.setNickname(obj.getString("nickname"));
                    if(obj.getString("friend_note")!=null){
                        user.setNote(obj.getString("friend_note"));
                    }
                    groupMemberData.add(user);
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                name.setText(json.getString("group_name"));
                                GridViewAdapter mGriViewAdapter=new GridViewAdapter(getApplicationContext(),groupMemberData);
                                gridView.setAdapter(mGriViewAdapter);
                                mGriViewAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }).start();
            }
        });
    }


}
