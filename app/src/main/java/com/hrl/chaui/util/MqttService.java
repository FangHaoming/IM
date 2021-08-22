package com.hrl.chaui.util;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.hrl.chaui.bean.User;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.File;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

public class MqttService extends Service {
    private final IBinder mBinder = new LocalBinder();
    private static final String TAG = "mqttService";
    private MqttByAli mqtt;
    private ArrayList<User> friReqMessage;  //好友申请消息队列
    private ArrayList<String> chatMessage;  //聊天消息队列

    public MqttService() {
    }

    public class LocalBinder extends Binder {
        public MqttService getService() {
            return MqttService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind");
        return mBinder;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        SharedPreferences sharedPreferences = getApplication().getSharedPreferences("data", MODE_PRIVATE);
        int user_id = sharedPreferences.getInt("user_id", -1);
        friReqMessage = (ArrayList<User>) JSONObject.parseArray(sharedPreferences.getString("friReqMessage", ""), User.class);
        String clientId = "GID_test@@@" + user_id;
        try {
            mqtt = new MqttByAli(clientId, "testtopic", new MyMqttCallback());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        SharedPreferences preferences = getApplication().getSharedPreferences("data", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("friReqMessage", JSONObject.toJSONString(friReqMessage));
        //editor.putString("chatMessage",JSONObject.toJSONString(chatMessage));
        editor.apply();
        try {
            mqtt.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        mqtt=null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        return START_STICKY;
    }

    public MqttByAli getMqtt() {
        return mqtt;
    }

    public void setMqtt(MqttByAli mqtt) {
        this.mqtt = mqtt;
    }

    public ArrayList<User> getFriReqMessage() {
        return friReqMessage;
    }

    public void setFriReqMessage(ArrayList<User> friReqMessage) {
        this.friReqMessage = friReqMessage;
    }

    public ArrayList<String> getChatMessage() {
        return chatMessage;
    }

    public void setChatMessage(ArrayList<String> chatMessage) {
        this.chatMessage = chatMessage;
    }

    public class MyMqttCallback implements MqttCallbackExtended {
        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            Log.e(TAG, "connectComplete");
        }

        @Override
        public void connectionLost(Throwable cause) {
            Log.e(TAG, "connectionLost");
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            String payloadString = new String(message.getPayload());
            JSONObject object = JSONObject.parseObject(payloadString);
            String msg = object.getString("msg");
            Log.e(TAG, "messageArrived:" + msg);
            switch (msg) {
                case "friendRequest":  //好友申请消息
                    //保存信息到文件中
                    User user = JSONObject.parseObject(payloadString, User.class);
                    friReqMessage.add(user);
                    break;
                case "p2pText":  //私聊文本消息
                    break;
                case "p2pFile":  //私聊文件消息
                    int total = object.getInteger("total");
                    int order = object.getInteger("order");
                    byte[] data =  object.getBytes("data");
                    if (total == 1 && order == 0)
                        FileUtils.bytesToFile(data, value.imgLocalPath, object.getString("name"));
                    else {
                        String hex = object.getString("hex");
                        if(order==0) {
                            HashMap<Integer, byte[]> map = new HashMap<>();
                            map.put(order, data);
                            FileCache.createNewCache(hex, map);
                        }
                        else FileCache.add2Cache(hex,order,data);
                        System.out.println(FileCache.getCount(hex)+"//"+total);
                        if (FileCache.getCount(hex)==total) {
                            String name = object.getString("name");
                            int length = object.getInteger("length");
                            File file=FileCache.mergeToFile(hex,total,length,value.imgLocalPath,name);
                            System.out.println("create file");
                            Log.e(TAG,"create file");
                        }
                    }
                    break;
            }

        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            Log.e(TAG, "deliveryComplete");
        }
    }

}