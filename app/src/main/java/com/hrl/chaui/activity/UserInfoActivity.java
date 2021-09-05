package com.hrl.chaui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
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
import com.aliyun.apsaravideo.sophon.bean.RTCAuthInfo;
import com.aliyun.apsaravideo.sophon.videocall.VideoCallActivity;
import com.aliyun.rtc.voicecall.bean.AliUserInfoResponse;
import com.aliyun.rtc.voicecall.ui.AliRtcChatActivity;
import com.bumptech.glide.Glide;
import com.hrl.chaui.R;
import com.hrl.chaui.bean.User;
import com.hrl.chaui.util.RTCHelper;

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
    public TextView add;
    public ImageView user_img;
    public LinearLayout sendMsg;
    public LinearLayout sendCall;
    public LinearLayout videoCall;
    public TextView setNote;
    public TextView delete;
    public User user;
    public SharedPreferences recv;
    public SharedPreferences.Editor editor;
    public Drawable  drawable;
    Intent intent;
    Bundle bundle;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable  Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recv = getSharedPreferences("data", Context.MODE_PRIVATE);
        editor = recv.edit();
        user=new User();
        intent=getIntent();
        bundle = intent.getExtras();
        if(bundle!=null){
            Log.i("isFriend in user", String.valueOf(bundle.getBoolean("isFriend")));
            if(bundle.getBoolean("isFriend")) {
                setContentView(R.layout.layout_user_info);
                back_arrow = findViewById(R.id.back_arrow);
                user_name = findViewById(R.id.user_name);
                sendMsg = findViewById(R.id.send_message);
                sendCall = findViewById(R.id.send_call);
                videoCall = findViewById(R.id.video_call);
                setNote = findViewById(R.id.setNote);
                delete = findViewById(R.id.delete);
                user_note = findViewById(R.id.user_note);
                user_phone = findViewById(R.id.user_phone);
                user_sign = findViewById(R.id.user_sign);
                user_img = findViewById(R.id.user_img);

                View.OnClickListener listener=new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch(v.getId()){
                            case R.id.setNote:
                                break;
                            case R.id.send_message:
                                Intent chatIntent = new Intent(UserInfoActivity.this, ChatActivity.class);
                                chatIntent.putExtra("targetUser",user);
                                startActivity(chatIntent);
                                break;
                            case R.id.delete:
                                break;
                            case R.id.send_call: {
                                Intent voiceCallIntent = new Intent(UserInfoActivity.this, AliRtcChatActivity.class);
                                String userClientID = "GID_test@@@" + recv.getInt("user_id", 0);
                                String targetClientID = "GID_test@@@" + user.getUser_id();
                                String channelID = RTCHelper.getChannelID(userClientID, targetClientID);
                                AliUserInfoResponse.AliUserInfo aliUserInfo = RTCHelper.getAliUserInfo(channelID, userClientID);
                                voiceCallIntent.putExtra("channel", channelID);
                                voiceCallIntent.putExtra("rtcAuthInfo", aliUserInfo);
                                voiceCallIntent.putExtra("user2Name", user.getUser_name());
                                startActivity(voiceCallIntent);
                                break;
                            }
                            case R.id.video_call: {
                                Intent videoCallIntent = new Intent(UserInfoActivity.this, VideoCallActivity.class);
                                String userClientID = "GID_test@@@" + recv.getInt("user_id", 0);
                                String targetClientID = "GID_test@@@" + user.getUser_id();
                                String channelID = RTCHelper.getNumsChannelID(userClientID, targetClientID);
                                RTCAuthInfo info = RTCHelper.getVideoCallRTCAuthInfo(channelID, userClientID);
                                String userName = recv.getString("user_name", "null");

                                videoCallIntent.putExtra("channel", channelID);
                                videoCallIntent.putExtra("username", userName);
                                videoCallIntent.putExtra("rtcAuthInfo", info);

                                startActivity(videoCallIntent);
                                break;
                            }
                            case R.id.back_arrow:
                                Intent intent = null;
                                if(bundle.getString("from").equals("group")){
                                    intent=new Intent(UserInfoActivity.this,GroupInfoActivity.class);
                                }else if(bundle.getString("from").equals("contact")){
                                    intent=new Intent(UserInfoActivity.this,MainActivity.class);
                                }else if(bundle.getString("from").equals("search")){
                                    intent=new Intent(UserInfoActivity.this,NewFriendActivity.class);
                                }
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                overridePendingTransition(0, R.anim.slide_right_out);
                                finish();
                                break;
                        }
                    }
                };
                sendMsg.setOnClickListener(listener);
                sendCall.setOnClickListener(listener);
                videoCall.setOnClickListener(listener);
                setNote.setOnClickListener(listener);
                delete.setOnClickListener(listener);
                back_arrow.setOnClickListener(listener);


                int friend_id = bundle.getInt("contact_id");
                if (friend_id == recv.getInt("user_id", 0)) {
                    Glide.with(UserInfoActivity.this).load(getResources().getString(R.string.app_prefix_img) + recv.getString("user_img", "")).into(user_img);
                    user_note.setText(recv.getString("user_name", ""));
                    user_sign.setText("个性签名: " + recv.getString("user_sign", ""));
                    user_phone.setText("手机号: " + recv.getString("user_phone", ""));
                } else{
                    sendByPost_friend(recv.getInt("user_id", 0), friend_id);
                }

            }
            else{
                setContentView(R.layout.layout_user_info_not);
                back_arrow=findViewById(R.id.back_arrow);
                user_name=findViewById(R.id.user_name);
                user_note=findViewById(R.id.user_nickname);//群昵称
                user_phone=findViewById(R.id.user_phone);
                user_sign=findViewById(R.id.user_sign);
                add=findViewById(R.id.add);
                if(bundle.getString("from").equals("group")){
                    if(bundle.getString("nickname")!=null){
                        user_note.setText("群昵称: "+bundle.getString("nickname"));
                    }
                    sendByPost_user(bundle.getString("user_phone"));
                }
                else if(bundle.getString("from").equals("search")){
                    JSONObject json=JSON.parseObject(bundle.getString("user_info"));
                    if(json.getString("user_name")!=null){
                        user_name.setText(json.getString("user_name"));
                    }
                    if(json.getString("user_phone")!=null){
                        user_phone.setText("手机号: "+json.getString("user_phone"));
                    }
                    if(json.getString("user_sign")!=null){
                        user_sign.setText(json.getString("user_sign"));
                    }
                }
                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO 发送添加好友请求
                    }
                });
                back_arrow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = null;
                        if(bundle.getString("from").equals("group")){
                            intent=new Intent(UserInfoActivity.this,GroupInfoActivity.class);
                        }else if(bundle.getString("from").equals("contact")){
                            intent=new Intent(UserInfoActivity.this,MainActivity.class);
                        }else if(bundle.getString("from").equals("search")){
                            intent=new Intent(UserInfoActivity.this,NewFriendActivity.class);
                        }
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        overridePendingTransition(0, R.anim.slide_right_out);
                        finish();
                    }
                });
            }
        }

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.white));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            Intent intent = null;
            if(bundle.getString("from").equals("group")){
                intent=new Intent(UserInfoActivity.this,GroupInfoActivity.class);
            }else if(bundle.getString("from").equals("contact")){
                intent=new Intent(UserInfoActivity.this,MainActivity.class);
            }else if(bundle.getString("from").equals("search")){
                intent=new Intent(UserInfoActivity.this,NewFriendActivity.class);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(0, R.anim.slide_right_out);
            finish();
        }
        return true;
    }

    private void sendByPost_friend(int user_id, int friend_id) {
        JSONObject json=new JSONObject();
        json.put("user_id",user_id);
        json.put("friend_id",friend_id);
        String path = getResources().getString(R.string.request_local)+"/friendSearch";
        OkHttpClient client = new OkHttpClient();
        final FormBody formBody = new FormBody.Builder()
                .add("json", json.toJSONString())
                .build();
        System.out.println("*********json friend"+json.toJSONString());
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
                user.setUser_name(json.getString("user_name"));
                user.setUser_gender(json.getString("user_gender"));
                user.setUser_phone(json.getString("user_phone"));
                user.setUser_sign(json.getString("user_sign"));
                user.setUser_img(json.getString("user_img"));
                user.setUser_id(json.getInteger("user_id"));
                user.setUser_note(json.getString("friend_note"));
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //根据用户性别设置图片
                                if(user.getUser_gender()!=null){
                                    if(user.getUser_gender().equals("女")){
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
                                if(user.getUser_note()==null){
                                    user_note.setText(user.getUser_name());
                                }
                                else{
                                    user_note.setText(user.getUser_note());
                                    user_name.setText("昵称: "+user.getUser_name());
                                }
                                if(user.getUser_phone()!=null){
                                    user_phone.setText("手机号: "+user.getUser_phone());
                                }
                                if(user.getUser_sign()!=null){
                                    user_sign.setText("个性签名: "+user.getUser_sign());
                                }
                                if(user.getUser_img()!=null){
                                    Glide.with(UserInfoActivity.this).load(getResources().getString(R.string.app_prefix_img)+user.getUser_img()).into(user_img);
                                }
                            }
                        });
                    }
                }).start();
            }
        });
    }

    private void sendByPost_user(String phone) {
        JSONObject json=new JSONObject();
        json.put("user_phone",phone);
        String path = getResources().getString(R.string.request_local)+"/userSearch";
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
                JSONObject json=JSON.parseObject(info);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(json.getString("user_name")!=null){
                            user_name.setText(json.getString("user_name"));
                        }
                        if(json.getString("user_phone")!=null){
                            user_phone.setText("手机号: "+json.getString("user_phone"));
                        }
                        if(json.getString("user_sign")!=null){
                            user_sign.setText(json.getString("user_sign"));
                        }
                    }
                });

            }
        });
    }
}
