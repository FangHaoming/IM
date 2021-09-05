package com.hrl.chaui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aliyun.rtc.voicecall.bean.AliUserInfoResponse;
import com.aliyun.rtc.voicecall.ui.AliRtcChatActivity;
import com.hrl.chaui.R;
import com.hrl.chaui.util.MqttByAli;
import com.hrl.chaui.util.MqttService;

import org.w3c.dom.Text;

public class AnswerVoiceCallActivity extends AppCompatActivity  {

    private ImageView callOk;
    private ImageView callCancel;
    private TextView callTarget;
    private String mChannelId;
    private AliUserInfoResponse.AliUserInfo mRtcAuthInfo;
    private String user2Name;
    private String targetClientID;

    private MqttByAli mqtt;
    private MqttServiceConnection connection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer_call);
        getDataFromIntent();

        // 绑定mqttService 获取 mqtt
        connection = new MqttServiceConnection();
        Intent intent = new Intent(this, MqttService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        Log.e("AnswerVoiceCallActivity", "mqtt: " + mqtt);

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
                mRtcAuthInfo = (AliUserInfoResponse.AliUserInfo) b.getSerializable("rtcAuthInfo");
                //用户名字
                user2Name = b.getString("user2Name");
                // 对方ClientID
                targetClientID = b.getString("targetClientID");
            }
        } else {
            Toast.makeText(this, "mChannelId:" + mChannelId + "\nuser2Name:" + user2Name + "\nmRtcAuthInfo:" + mRtcAuthInfo, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initView() {
        callOk = (ImageView) findViewById(R.id.call_ok);
        callCancel = (ImageView) findViewById(R.id.call_cancel);
        callTarget = (TextView) findViewById(R.id.call_target);

        callTarget.setText("呼叫来自: " + user2Name);

        callOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 确认就进入AliRtcChatActivity
                Intent intent = new Intent(AnswerVoiceCallActivity.this, AliRtcChatActivity.class);
                intent.putExtra("channel", mChannelId);
                intent.putExtra("rtcAuthInfo", mRtcAuthInfo);
                intent.putExtra("user2Name", user2Name);
                startActivity(intent);
                // 进入后结束当前Activity
                finish();
            }
        });


        callCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 发回拒绝消息
                if (mqtt != null) {
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