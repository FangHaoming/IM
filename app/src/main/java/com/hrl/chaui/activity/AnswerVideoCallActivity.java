package com.hrl.chaui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aliyun.apsaravideo.sophon.bean.RTCAuthInfo;
import com.aliyun.apsaravideo.sophon.videocall.VideoCallActivity;
import com.aliyun.rtc.voicecall.bean.AliUserInfoResponse;
import com.aliyun.rtc.voicecall.ui.AliRtcChatActivity;
import com.hrl.chaui.R;
import com.hrl.chaui.util.MqttByAli;
import com.hrl.chaui.util.MqttService;

public class AnswerVideoCallActivity extends AppCompatActivity {


    private ImageView callOk;
    private ImageView callCancel;
    private TextView callTarget;
    private String mChannelId;
    private RTCAuthInfo mRtcAuthInfo;
    private String userName;
    private String targetName;
    private String targetClientID;
    private MqttByAli mqtt;
    private MqttServiceConnection connection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer_video_call);
        getDataFromIntent();

        // 绑定服务，获取mqtt
        connection = new MqttServiceConnection();
        Intent intent = new Intent(this, MqttService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            Bundle b = intent.getExtras();
            if (b != null) {
                //频道号
                mChannelId = b.getString("channel");
                //用户信息
                mRtcAuthInfo = (RTCAuthInfo) b.getSerializable("rtcAuthInfo");
                //用户名字
                userName = b.getString("username");
                // 对方的ClientID
                targetClientID = b.getString("targetClientID");
                // 对方的名字
                targetName = b.getString("targetName");
            }
        } else {
            Toast.makeText(this, "mChannelId:" + mChannelId + "\nusername:" + userName + "\nmRtcAuthInfo:" + mRtcAuthInfo, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initView() {
        callOk = (ImageView) findViewById(R.id.call_ok);
        callCancel = (ImageView) findViewById(R.id.call_cancel);
        callTarget = (TextView) findViewById(R.id.call_target);
        callTarget.setText("呼叫来自: " + targetName);

        callOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 确认就进入AliRtcChatActivity
                Intent intent = new Intent(AnswerVideoCallActivity.this, VideoCallActivity.class);
                intent.putExtra("channel", mChannelId);
                intent.putExtra("rtcAuthInfo", mRtcAuthInfo);
                intent.putExtra("username", userName);
                startActivity(intent);
                finish();
            }
        });


        callCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 发回拒绝消息
                if (mqtt != null){
                    mqtt.sendP2PCallRequestCancel(targetClientID);
                }
                finish();
            }
        });
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
}