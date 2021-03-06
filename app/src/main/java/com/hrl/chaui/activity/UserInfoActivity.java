package com.hrl.chaui.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.hrl.chaui.util.MqttByAli;
import com.hrl.chaui.util.MqttService;
import com.hrl.chaui.util.RTCHelper;
import com.hrl.chaui.util.http;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.hrl.chaui.MyApplication.modifyUser;
import static com.hrl.chaui.util.Constant.CHECKONLINEERR;
import static com.hrl.chaui.util.Constant.NOTONLINE;

public class UserInfoActivity extends AppCompatActivity {
    public TextView back_arrow;
    public TextView user_name;
    public TextView user_note;
    public TextView user_phone;
    public TextView user_sign;
    public TextView add;
    public CircleImageView user_img;
    public LinearLayout sendMsg;
    public LinearLayout sendCall;
    public LinearLayout videoCall;
    public TextView setNote;
    public TextView delete;
    public User user;
    public SharedPreferences sp;
    public SharedPreferences.Editor editor;
    public Drawable  drawable;
    Intent intent;
    Bundle bundle;

    // ????????????????????????
    private NoOnlineHandler noOnlineHandler;
    private MqttByAli mqtt;
    private MqttServiceConnection connection;



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable  Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initMqtt();
        SharedPreferences userId=getSharedPreferences("data_userID",MODE_PRIVATE); //??????ID??????
        sp=getSharedPreferences("data_"+userId.getInt("user_id",-1),MODE_PRIVATE); //??????ID????????????????????????
        editor = sp.edit();
        user=new User();
        intent=getIntent();
        bundle = intent.getExtras();
        if(bundle!=null){
            Log.i("who in UserInfoActivity", bundle.getString("who"));
            if(bundle.getString("who").equals("friend")) {
                // ??????????????????
                setContentView(R.layout.layout_user_info_friend);
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

                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (v.getId()) {
                            case R.id.setNote:
                                Intent intent_data=new Intent(UserInfoActivity.this,ModifyNameActivity.class);
                                Bundle data=new Bundle();
                                data.putString("from","friend");
                                data.putString("friend_from",bundle.getString("friend_from"));
                                data.putInt("contact_id",bundle.getInt("contact_id"));
                                data.putString("friend_note",bundle.getString("friend_note"));
                                intent_data.putExtras(data);
                                startActivity(intent_data);
                                break;
                            case R.id.delete:
                                sendByPost_delete(modifyUser.getUser_id(),bundle.getInt("contact_id"));
                                break;
                            case R.id.send_message:
                                Intent chatIntent = new Intent(UserInfoActivity.this, ChatActivity.class);
                                chatIntent.putExtra("targetUser", user);
                                startActivity(chatIntent);
                                finish();
                                break;
                            case R.id.send_call: {

                                // ?????????????????????clientID
                                String userClientID = "GID_test@@@" + sp.getInt("user_id", 0);
                                String targetClientID = "GID_test@@@" + user.getUser_id();
                                String channelID = RTCHelper.getChannelID(userClientID, targetClientID);
                                AliUserInfoResponse.AliUserInfo aliUserInfo = RTCHelper.getAliUserInfo(channelID, userClientID);
                                Intent voiceCallIntent = new Intent(UserInfoActivity.this, AliRtcChatActivity.class);

                                voiceCallIntent.putExtra("channel", channelID);
                                voiceCallIntent.putExtra("rtcAuthInfo", aliUserInfo);
                                voiceCallIntent.putExtra("user2Name", user.getUser_name());

                                // ????????????????????????
                                new Thread(() -> {
                                    try {
                                        boolean isOnline = MqttByAli.checkIsOnline(targetClientID);
                                        if (isOnline) {
                                            Log.e("UserInfoActivity", "???targetClientID:" + targetClientID + " ????????????????????????" + " mqtt:" + mqtt);
                                            mqtt.sendP2PVoiceCallRequest(targetClientID);
                                            startActivity(voiceCallIntent);
                                        } else {
                                            Message message = new Message();
                                            message.what = NOTONLINE;
                                            noOnlineHandler.sendMessage(message);
                                        }
                                    } catch (Exception e) {
                                        Message message = new Message();
                                        message.what = CHECKONLINEERR;
                                        noOnlineHandler.sendMessage(message);
                                        e.printStackTrace();
                                    }
                                }).start();

                                break;
                            }
                            case R.id.video_call: {

                                Intent videoCallIntent = new Intent(UserInfoActivity.this, VideoCallActivity.class);
                                String userClientID = "GID_test@@@" + sp.getInt("user_id", 0);
                                String targetClientID = "GID_test@@@" + user.getUser_id();
                                String channelID = RTCHelper.getNumsChannelID(userClientID, targetClientID);
                                RTCAuthInfo info = RTCHelper.getVideoCallRTCAuthInfo(channelID, userClientID);
                                String userName = sp.getString("user_name", "null");

                                videoCallIntent.putExtra("channel", channelID);
                                videoCallIntent.putExtra("username", userName);
                                videoCallIntent.putExtra("rtcAuthInfo", info);


                                new Thread(() -> {
                                    try {
                                        boolean isOnline = MqttByAli.checkIsOnline(targetClientID);
                                        if (isOnline) {
                                            Log.e("UserInfoActivity", "targetClientID:" + targetClientID + " ????????????????????????");
                                            mqtt.sendP2PVideoCallRequest(targetClientID);
                                            startActivity(videoCallIntent);
                                        } else {
                                            Message message = new Message();
                                            message.what = NOTONLINE;
                                            noOnlineHandler.sendMessage(message);
                                        }
                                    } catch (Exception e) {
                                        Message message = new Message();
                                        message.what = CHECKONLINEERR;
                                        noOnlineHandler.sendMessage(message);
                                        e.printStackTrace();
                                    }
                                }).start();
                                break;
                            }
                            case R.id.back_arrow:
                                back();
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

                // ????????????????????????????????????????????????
                sendByPost_friend(modifyUser.getUser_id(), bundle.getInt("contact_id"));
            }
            else if(bundle.getString("who").equals("stranger")){
                // ?????????????????????
                setContentView(R.layout.layout_user_info_stranger);
                back_arrow=findViewById(R.id.back_arrow);
                user_name=findViewById(R.id.user_name);
                user_note=findViewById(R.id.user_nickname);//?????????
                user_phone=findViewById(R.id.user_phone);
                user_sign=findViewById(R.id.user_sign);
                user_img=findViewById(R.id.user_img);
                add=findViewById(R.id.add);
                if(bundle.getString("from").equals("group")){
                    if(bundle.getString("nickname")!=null){
                        user_note.setText("?????????: "+bundle.getString("nickname"));
                    }
                    sendByPost_user(bundle.getString("user_phone"));
                }
                else if(bundle.getString("from").equals("search")){
                    JSONObject json=JSON.parseObject(bundle.getString("user_info"));
                    bundle.putInt("contact_id",json.getInteger("user_id"));
                    if(json.getString("user_gender")!=null){
                        if(json.getString("user_gender").equals("???")){
                            drawable = getResources().getDrawable(R.drawable.female);
                        }
                        else{
                            drawable= getResources().getDrawable(R.drawable.male);
                        }
                    }
                    else{
                        drawable= getResources().getDrawable(R.drawable.unknown);
                    }
                    if(json.getString("user_name")!=null){
                        user_name.setText(json.getString("user_name"));
                        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                        user_name.setCompoundDrawables(null,null, drawable,null);
                    }
                    if(json.getString("user_phone")!=null){
                        user_phone.setText("?????????: "+json.getString("user_phone"));
                    }
                    if(json.getString("user_sign")!=null){
                        user_sign.setText(json.getString("user_sign"));
                    }
                }
                add.setOnClickListener(new View.OnClickListener() {  //??????????????????
                    @Override
                    public void onClick(View v) {
                        Intent intent_Add=new Intent(UserInfoActivity.this,AddNewFriendActivity.class);
                        Bundle bundle_Add=new Bundle();
                        if(bundle.getString("from").equals("group")){
                            bundle_Add.putString("group_name",bundle.getString("group_name"));
                            bundle_Add.putString("from","group");
                        }else{
                            bundle_Add.putString("from","search");
                        }
                        bundle_Add.putInt("targetClientID",bundle.getInt("contact_id"));

                        intent_Add.putExtras(bundle_Add);
                        startActivity(intent_Add);
                    }
                });
                back_arrow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        back();
                    }
                });
            }
            else if(bundle.getString("who").equals("me")){ //??????
                setContentView(R.layout.layout_user_info_me);
                if(sp.getString("user_gender","").equals("???")){
                    drawable = getResources().getDrawable(R.drawable.female);
                }
                else{
                    drawable= getResources().getDrawable(R.drawable.male);
                }
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                user_name=findViewById(R.id.user_name);
                user_name.setCompoundDrawables(null,null, drawable,null);
                user_note=findViewById(R.id.user_nickname);
                user_phone=findViewById(R.id.user_phone);
                user_sign=findViewById(R.id.user_sign);
                user_img=findViewById(R.id.user_img);
                sendMsg = findViewById(R.id.send_message);
                back_arrow=findViewById(R.id.back_arrow);
                Glide.with(UserInfoActivity.this).load(getResources().getString(R.string.app_prefix_img) + sp.getString("user_img", "")).into(user_img);
                user_name.setText(sp.getString("user_name", ""));
                user_sign.setText(sp.getString("user_sign", ""));
                user_note.setText("?????????: "+bundle.getString("nickname"));
                user_phone.setText("?????????: " + sp.getString("user_phone", ""));
                sendMsg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent chatIntent = new Intent(UserInfoActivity.this, ChatActivity.class);
                        user.setUser_name(sp.getString("user_name",""));
                        user.setUser_pwd(sp.getString("user_pwd",""));
                        user.setUser_img(sp.getString("user_img",""));
                        user.setUser_phone(sp.getString("user_phone",""));
                        user.setUser_id(sp.getInt("user_id",-1));
                        chatIntent.putExtra("targetUser", user);
                        startActivity(chatIntent);
                        finish();
                    }
                });
                back_arrow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        back();
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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent=getIntent();
        Bundle bundle_note=intent.getExtras();
        //Bundle bundle_return=intent.getExtras();
        if(bundle_note!=null){
            if(bundle_note.getBoolean("isModify")){
                sendByPost_friendnote(modifyUser.getUser_id(),bundle_note.getInt("contact_id"),bundle_note.getString("friend_note"),0);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            back();
        }
        return true;
    }

    private void back(){
        Intent intent = null;
        if(bundle!=null){
            if(bundle.getString("from").equals("group")){
                intent=new Intent(UserInfoActivity.this,GroupInfoActivity.class);
            }else if(bundle.getString("from").equals("contact")||bundle.getString("from").equals("modifyName")){
                intent=new Intent(UserInfoActivity.this,MainActivity.class);
            }else if(bundle.getString("from").equals("search")){
                intent=new Intent(UserInfoActivity.this,NewFriendActivity.class);
            }else if(bundle.getString("from").equals("chat")){
                intent=new Intent(UserInfoActivity.this,ChatActivity.class);
            }
        }else{
            intent=new Intent(UserInfoActivity.this,MainActivity.class);
        }
        Objects.requireNonNull(intent).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(0, R.anim.slide_right_out);
        finish();
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
                Toast.makeText(UserInfoActivity.this, "?????????????????????", Toast.LENGTH_SHORT).show();
                Looper.loop();
                e.printStackTrace();
            }
            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                String info=response.body().string();
                System.out.println("***********info_this"+info);
                JSONObject json= JSON.parseObject(info);
                user=JSON.parseObject(info,User.class);
                if(info.contains("friend_note")){
                    user.setUser_note(json.getString("friend_note"));
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //??????????????????????????????
                                if(user.getUser_gender()!=null){
                                    if(user.getUser_gender().equals("???")){
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
                                    user_name.setText("??????: "+user.getUser_name());
                                }
                                if(user.getUser_phone()!=null){
                                    user_phone.setText("?????????: "+user.getUser_phone());
                                }
                                if(user.getUser_sign()!=null){
                                    user_sign.setText("????????????: "+user.getUser_sign());
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
                Toast.makeText(UserInfoActivity.this, "?????????????????????", Toast.LENGTH_SHORT).show();
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
                        if(json.getString("user_gender")!=null){
                            if(json.getString("user_gender").equals("???")){
                                drawable = getResources().getDrawable(R.drawable.female);
                            }
                            else{
                                drawable= getResources().getDrawable(R.drawable.male);
                            }
                        }
                        else{
                            drawable= getResources().getDrawable(R.drawable.unknown);
                        }
                        if(json.getString("user_name")!=null){
                            user_name.setText(json.getString("user_name"));
                            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                            user_name.setCompoundDrawables(null,null, drawable,null);
                        }
                        if(json.getString("user_phone")!=null){
                            user_phone.setText("?????????: "+json.getString("user_phone"));
                        }
                        if(json.getString("user_sign")!=null){
                            user_sign.setText(json.getString("user_sign"));
                        }
                        if(json.getString("user_img")!=null){
                            Glide.with(UserInfoActivity.this).load(getResources().getString(R.string.app_prefix_img)+json.getString("user_img")).into(user_img);
                        }
                    }
                });

            }
        });
    }

    private void sendByPost_friendnote(int user_id,int friend_id, String friend_note,Integer notice_rank) {
        JSONObject json=new JSONObject();
        json.put("friend_id",friend_id);
        json.put("user_id",user_id);
        json.put("friend_note",friend_note);
        json.put("notice_rank",notice_rank);
        String path = getResources().getString(R.string.request_local)+"/friendUpdate";
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
                Toast.makeText(UserInfoActivity.this, "?????????????????????", Toast.LENGTH_SHORT).show();
                Looper.loop();
                e.printStackTrace();
            }
            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                String info = response.body().string();
                System.out.println("***********group_info" + info);
                JSONObject json = JSON.parseObject(info);
                if(Objects.equals(json.get("msg"), "update success")){
                    http.sendByPost(UserInfoActivity.this,modifyUser.getUser_id());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            user_note.setText(friend_note);
                        }
                    });
                    Looper.prepare();
                    Toast.makeText(UserInfoActivity.this,"????????????!",Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }else{
                    Looper.prepare();
                    Toast.makeText(UserInfoActivity.this,"????????????",Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }


            }
        });
    }
    private void sendByPost_delete(int user_id, int friend_id) {
        JSONObject json=new JSONObject();
        json.put("friend_id",friend_id);
        json.put("user_id",user_id);
        String path = getResources().getString(R.string.request_local)+"/friendDelete";
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
                Toast.makeText(UserInfoActivity.this, "?????????????????????", Toast.LENGTH_SHORT).show();
                Looper.loop();
                e.printStackTrace();
            }
            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                String info = response.body().string();
                System.out.println("***********group_info" + info);
                JSONObject json = JSONObject.parseObject(info);
                if(json.getString("msg").equals("delete success")){
                    Looper.prepare();
                    http.sendByPost(UserInfoActivity.this,modifyUser.getUser_id());
                    Toast.makeText(UserInfoActivity.this, "????????????!", Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(UserInfoActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    Looper.loop();
                }else{
                    Looper.prepare();
                    Toast.makeText(UserInfoActivity.this, "????????????", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }

            }
        });
    }


    private static class NoOnlineHandler extends  Handler {
        private final WeakReference<UserInfoActivity> mTarget;

        private NoOnlineHandler(UserInfoActivity activity) {
            mTarget = new WeakReference<UserInfoActivity>(activity);
        }


        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case NOTONLINE:
                    Toast.makeText(mTarget.get(), "???????????????", Toast.LENGTH_SHORT).show();
                    break;
                case CHECKONLINEERR:
                    Toast.makeText(mTarget.get(), "?????????????????????????????????",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    private class MqttServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MqttService.LocalBinder localBinder = (MqttService.LocalBinder) service;
            mqtt = localBinder.getService().getMqtt();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ??????MqttService
        unbindService(connection);
        if(user_img != null &&  !this.isDestroyed()){
            Glide.with(this).clear(user_img);
            user_img = null;
        }
    }

    private void initMqtt() {
        // ????????????????????????????????????????????????
        noOnlineHandler = new NoOnlineHandler(UserInfoActivity.this);

        // ??????MqttService ?????? MqttByAli????????? mqtt
        Intent mqttServiceIntent = new Intent(this, MqttService.class);
        startService(mqttServiceIntent); // ???????????????????????????
        connection = new MqttServiceConnection();
        bindService(mqttServiceIntent, connection, Context.BIND_AUTO_CREATE);
        Log.e("UserInfoActivity", "mqtt?????????" + mqtt);
    }

}
