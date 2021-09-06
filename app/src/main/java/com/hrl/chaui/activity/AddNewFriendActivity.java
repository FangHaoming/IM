package com.hrl.chaui.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hrl.chaui.R;
import com.hrl.chaui.util.MqttByAli;
import com.hrl.chaui.util.MqttService;

import static com.hrl.chaui.MyApplication.modifyUser;

public class AddNewFriendActivity extends AppCompatActivity {

    private TextView back_arrow;
    private EditText check;
    private Button send;
    private MqttByAli mqtt = null;
    private MqttServiceConnection connection = null;
    @Override
    protected void onCreate(@Nullable  Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_add_friend);
        back_arrow=findViewById(R.id.back_arrow);
        check=findViewById(R.id.check);
        send=findViewById(R.id.send);
        Bundle bundle=getIntent().getExtras();
        check.setText("我是来自群聊"+"\""+bundle.getString("group_name")+"\"的"+modifyUser.getUser_name());

        String targetClientID = "GID_test@@@" + bundle.getInt("targetClientID");
        Intent intent=new Intent(AddNewFriendActivity.this,UserInfoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Intent mqttServiceIntent = new Intent(this, MqttService.class);
        startService(mqttServiceIntent);
        connection = new MqttServiceConnection();
        // 绑定MqttService 获取 MqttByAli对象
        bindService(mqttServiceIntent, connection, Context.BIND_AUTO_CREATE);

        back_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent);
                finish();
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                modifyUser.setCheck(check.getText().toString());
                mqtt.friendRequest(modifyUser,targetClientID);
                Toast.makeText(AddNewFriendActivity.this,"发送成功",Toast.LENGTH_SHORT).show();
                startActivity(intent);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }
}
