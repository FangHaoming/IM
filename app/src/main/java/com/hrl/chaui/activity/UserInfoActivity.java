package com.hrl.chaui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.hrl.chaui.R;
import com.hrl.chaui.bean.User;

import java.io.IOException;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UserInfoActivity extends AppCompatActivity {
    public TextView back_arrow;
    public TextView user_name;
    public TextView user_note;
    public TextView user_phone;
    public TextView user_sign;
    public ImageView user_img;
    public LinearLayout sendMsg;
    public LinearLayout sendCall;
    public TextView setNote;
    public TextView delete;
    public User user;
    public SharedPreferences recv;
    public SharedPreferences.Editor editor;
    public Drawable  drawable;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable  Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_user_info);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.white));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        back_arrow=findViewById(R.id.back_arrow);
        user_name=findViewById(R.id.user_name);
        sendMsg=findViewById(R.id.send_message);
        sendCall=findViewById(R.id.send_call);
        setNote=findViewById(R.id.setNote);
        delete=findViewById(R.id.delete);
        user_note=findViewById(R.id.user_note);
        user_phone=findViewById(R.id.user_phone);
        user_sign=findViewById(R.id.user_sign);
        user_img=findViewById(R.id.user_img);

        user=new User();

        recv = getSharedPreferences("data", Context.MODE_PRIVATE);
        editor = recv.edit();
        Intent intent=getIntent();
        Bundle bundle = intent.getExtras();
        int friend_id=bundle.getInt("contact_id");
        sendByPost(recv.getInt("user_id",0),friend_id);
        UserInfoActivity userInfoActivity = this;

        View.OnClickListener listener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(v.getId()){
                    case R.id.setNote:
                        break;
                    case R.id.delete:
                        break;
                    case R.id.send_message:
                        Intent chatIntent = new Intent(userInfoActivity, ChatActivity.class);
                        chatIntent.putExtra("user_id", user.getId());
                        chatIntent.putExtra("user_name", user.getName());
                        chatIntent.putExtra("user_gender", user.getGender());
                        chatIntent.putExtra("user_phone", user.getPhone());
                        chatIntent.putExtra("user_sign", user.getSign());
                        chatIntent.putExtra("user_img", user.getImg());
                        chatIntent.putExtra("friend_note", user.getNote());
                        startActivity(chatIntent);
                        break;
                    case R.id.send_call:
                        break;
                    case R.id.back_arrow:
                        Intent intent=new Intent(UserInfoActivity.this,MainActivity.class);
                        startActivity(intent);
                        overridePendingTransition(0, R.anim.slide_right_out);
                        finish();
                        break;
                }
            }
        };
        sendMsg.setOnClickListener(listener);
        sendCall.setOnClickListener(listener);
        setNote.setOnClickListener(listener);
        delete.setOnClickListener(listener);
        back_arrow.setOnClickListener(listener);
    }
    private void sendByPost(int user_id, int friend_id) {
        JSONObject json=new JSONObject();
        json.put("user_id",user_id);
        json.put("friend_id",friend_id);
        String path = "http://40f730q296.qicp.vip/friendSearch";
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
                Toast.makeText(UserInfoActivity.this, "服务器连接失败", Toast.LENGTH_SHORT).show();
                Looper.loop();
                e.printStackTrace();
            }
            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                String info=response.body().string();
                System.out.println("***********info_this"+info);
                JSONObject json= JSON.parseObject(info);
                user.setName(json.getString("user_name"));
                user.setGender(json.getString("user_gender"));
                user.setPhone(json.getString("user_phone"));
                user.setSign(json.getString("user_sign"));
                user.setImg(json.getString("user_img"));
                user.setId(json.getInteger("user_id"));
                user.setNote(json.getString("friend_note"));
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //根据用户性别设置图片
                                if(user.getGender()!=null){
                                    if(user.getGender().equals("女")){
                                        drawable = getResources().getDrawable(R.drawable.female);
                                    }
                                    else{
                                        drawable= getResources().getDrawable(R.drawable.male);
                                    }
                                }
                                else{
                                    drawable= getResources().getDrawable(R.drawable.unknown);
                                }

                                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                                user_note.setCompoundDrawables(null,null, drawable,null);
                                if(user.getNote()==null){
                                    user_note.setText(user.getName());
                                }
                                else{
                                    user_note.setText(user.getNote());
                                    user_name.setText("昵称: "+user.getName());
                                }
                                if(user.getPhone()!=null){
                                    user_phone.setText("手机号: "+user.getPhone());
                                }
                                if(user.getSign()!=null){
                                    user_sign.setText("个性签名: "+user.getSign());
                                }
                                if(user.getImg()!=null){
                                    Glide.with(UserInfoActivity.this).load(getResources().getString(R.string.app_prefix_img)+user.getImg()).into(user_img);
                                }
                            }
                        });
                    }
                }).start();
            }
        });
    }
}
