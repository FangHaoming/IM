package com.hrl.chaui.util;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.apsaravideo.sophon.bean.RTCAuthInfo;
import com.aliyun.rtc.voicecall.bean.AliUserInfoResponse;
import com.hrl.chaui.activity.AnswerVideoCallActivity;
import com.hrl.chaui.activity.AnswerVoiceCallActivity;
import com.hrl.chaui.bean.AudioMsgBody;
import com.hrl.chaui.bean.FileMsgBody;
import com.hrl.chaui.bean.ImageMsgBody;
import com.hrl.chaui.bean.Message;
import com.hrl.chaui.bean.MsgSendStatus;
import com.hrl.chaui.bean.MsgType;
import com.hrl.chaui.bean.TextMsgBody;
import com.hrl.chaui.bean.User;
import com.hrl.chaui.bean.VideoMsgBody;
import com.hrl.chaui.dao.imp.MessageDaoImp;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static com.hrl.chaui.MyApplication.getUserFromContactData;
import static com.hrl.chaui.MyApplication.groupData;

public class MqttService extends Service {
    private final IBinder mBinder = new LocalBinder();
    private static final String TAG = "mqttService";
    private MqttByAli mqtt;
    private ArrayList<User> friReqMessage;  //好友申请消息队列
    private ArrayList<String> chatMessage;  //聊天消息队列
    public static final String MESSAGEARRIVEACTION = "MESSAGEARRIVEACTION";
    private MessageDaoImp messageDao = MessageDaoImp.getInstance();
    private String clientID = null;
    private int groupChatQos = 1;

    public MqttService() {
    }

    public class LocalBinder extends Binder {
        public MqttService getService() {
            return MqttService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "MqttService被绑定了，onBind:" + intent.getAction());
        return mBinder;
    }

    @Override
    public void onCreate() {
        SharedPreferences sharedPreferences = getApplication().getSharedPreferences("data", MODE_PRIVATE);
        int user_id = sharedPreferences.getInt("user_id", -1);
        friReqMessage = (ArrayList<User>) JSONObject.parseArray(sharedPreferences.getString("friReqMessage", ""), User.class);
        clientID = "GID_test@@@" + user_id;

        try {
            mqtt = new MqttByAli(clientID, "testtopic", new MyMqttCallback());

            // 订阅所有群聊
            String[] topicFilters = new String[groupData.size()];
            int qos[] = new int[groupData.size()];

            for (int i = 0; i < topicFilters.length; i++) {
                topicFilters[i] =String.valueOf(groupData.get(i).getUser_id());
                qos[i] = groupChatQos;
            }
            mqtt.subscribe(topicFilters,qos);

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
        Log.e(TAG, "onStartCommand()");
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
            Log.e(TAG, "connectionLost + cause: " + cause);
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            Log.e(TAG, "messageArrived() + topic:" + topic + " message:" + message);

            String payloadString = new String(message.getPayload());
            JSONObject object = JSONObject.parseObject(payloadString);
            Message localMessage =new Message();

            // 获取发送时间
            long sendTime = object.getLong("sendTime");

            // 发送方clientID
            String sendID = object.getString("senderID");

            // P2P的topic: parentTopic/p2p/clientID
            // 群聊的topic: parentTopic/groupChat/groupID
            String targetID = null;
            if (topic.split("/")[1].equals("p2p")) { // 私聊消息
                localMessage.setGroup(false);
                targetID = clientID;
            } else { // 群聊消息
                targetID = topic.split("/")[2];
                localMessage.setGroup(true);

                // 对收到的群聊消息进行过滤 （当该消息是当前用户发送时，不用处理）
                if (sendID.equals(clientID)) {
                    Log.e(TAG, "收到自己发的群聊信息");
                    return;
                }
            }

            // 接收对方发来的信息
            localMessage.setUuid(UUID.randomUUID() + "");
            localMessage.setSenderId(sendID);
            localMessage.setTargetId(targetID);
            localMessage.setSentStatus(MsgSendStatus.SENDING);
            localMessage.setSentTime(sendTime);
            localMessage.setCheck(false);

            String msg = object.getString("msg");

            Log.e(TAG, "messageArrived:" + msg + "message:" + message);
            switch (msg) {
                case "friendRequest":  //好友申请消息
                    //保存信息到文件中
                    User user = JSONObject.parseObject(payloadString, User.class);
                    friReqMessage.add(user);
                    break;

                case "Text": {
                    //私聊文本消息
                    // 接收消息
                    byte[] dataText = object.getBytes("data");
                    String text = new String(dataText); // 该文本就是私聊文本消息
                    localMessage.setMsgType(MsgType.TEXT);
                    TextMsgBody textMsgBody = new TextMsgBody();
                    textMsgBody.setMessage(text);
                    localMessage.setBody(textMsgBody);
                    // 将Message存储在数据库中
                    messageDao.insertMessage(MqttService.this, localMessage);
                    // 发出广播
                    sendMessageBroadcast(localMessage);
                    break;
                }
                case "File":  {
                    //私聊文件消息
                    File file = receiveFile(object);
                    if (file != null) {
                        // 文件接收完后
                        localMessage.setMsgType(MsgType.FILE);
                        FileMsgBody mFileMsgBody = new FileMsgBody();
                        mFileMsgBody.setDisplayName(file.getName());
                        mFileMsgBody.setSize(file.length());
                        mFileMsgBody.setLocalPath(file.getPath());
                        localMessage.setBody(mFileMsgBody);
                        // 将Message存储在数据库中
                        messageDao.insertMessage(MqttService.this, localMessage);

                        // 发出广播
                        sendMessageBroadcast(localMessage);
                    }
                    break;
                }
                case "Image" : {
                    // 私聊图片消息
                    File fileImage = receiveFile(object);
                    if(fileImage != null) {
                        // 文件接收完后
                        localMessage .setMsgType(MsgType.IMAGE);
                        ImageMsgBody mImageMsgBody = ImageMsgBody.obtain(fileImage.getPath(), fileImage.getPath(), true);
                        localMessage.setBody(mImageMsgBody);
                        // 将Message存储在数据库中
                        messageDao.insertMessage(MqttService.this, localMessage);

                        // 发出广播
                        sendMessageBroadcast(localMessage);
                    }
                    break;
                }
                case "Video" : {
                    File fileVideo = receiveFile(object);
                    if (fileVideo != null) {

                        localMessage.setMsgType(MsgType.VIDEO);
                        VideoMsgBody videoMsgBody = new VideoMsgBody();
                        videoMsgBody.setDisplayName(fileVideo.getName());
                        videoMsgBody.setSize(fileVideo.length());
                        videoMsgBody.setLocalPath(fileVideo.getPath());

                        // 获取缩略图
                        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                        mediaMetadataRetriever.setDataSource(fileVideo.getPath());
                        Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime();
                        String imgname = System.currentTimeMillis() + ".jpg";
                        String urlpath = value.imgLocalPath + "/" + imgname;
                        File f = new File(urlpath);
                        try {
                            if (f.exists()) {
                                f.delete();
                            }
                            FileOutputStream out = new FileOutputStream(f);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                            out.flush();
                            out.close();
                        } catch (Exception e) {
                            LogUtil.d("视频缩略图路径获取失败：" + e.toString());
                            e.printStackTrace();
                        }

                        videoMsgBody.setExtra(urlpath);
                        localMessage.setBody(videoMsgBody);

                        // 将Message存储在数据库中
                        messageDao.insertMessage(MqttService.this, localMessage);

                        // 发出广播
                        sendMessageBroadcast(localMessage);
                    }
                    break;
                }
                case "Audio" : {
                    File fileAudio = receiveFile(object);
                    int time = object.getIntValue("time"); // 语音的时间
                    if (fileAudio != null) {
                        localMessage.setMsgType(MsgType.AUDIO);
                        AudioMsgBody audioMsgBody = new AudioMsgBody();
                        audioMsgBody.setDuration(time);
                        audioMsgBody.setLocalPath(fileAudio.getPath());
                        localMessage.setBody(audioMsgBody);
                        // 将Message存储在数据库中
                        messageDao.insertMessage(MqttService.this, localMessage);

                        // 发出广播
                        sendMessageBroadcast(localMessage);
                    }
                    break;
                }
                case "VoiceCall" :{
                    // 跳转到 接听 和 拒绝接听的Activity
                    Intent intent = new Intent(MqttService.this, AnswerVoiceCallActivity.class);

                    // channelID
                    String userClientID = localMessage.getTargetId();
                    String targetClientID = localMessage.getSenderId();
                    String channelID = RTCHelper.getChannelID(userClientID, targetClientID);
                    AliUserInfoResponse.AliUserInfo aliUserInfo = RTCHelper.getAliUserInfo(channelID, userClientID);

                    // 从通讯录获取目标用户
                    User targetUser = getUserFromContactData(targetClientID);
                    String name = targetUser == null ? "unknown" : targetUser.getUser_name();

                    intent.putExtra("channel", channelID);
                    intent.putExtra("rtcAuthInfo", aliUserInfo);
                    intent.putExtra("user2Name", name);
                    intent.putExtra("targetClientID", targetClientID);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


                    // 判断一下对方是否在线，如果在线就进入判断是否接听的界面

                    new Thread(()->{
                        try {
                            boolean isOnline = MqttByAli.checkIsOnline(targetClientID);
                            if (isOnline) {
                                startActivity(intent);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();

                    break;
                }
                case "VideoCall" : {
                    Intent intent = new Intent(MqttService.this, AnswerVideoCallActivity.class);
                    // channelID
                    String userClientID = localMessage.getTargetId();
                    String targetClientID = localMessage.getSenderId();
                    String channelID = RTCHelper.getNumsChannelID(userClientID, targetClientID);
                    RTCAuthInfo info = RTCHelper.getVideoCallRTCAuthInfo(channelID, userClientID);

                    User targetUser = getUserFromContactData(targetClientID);
                    String name = targetUser == null ? "unknown" : targetUser.getUser_name();

                    SharedPreferences recv = getSharedPreferences("data", Context.MODE_PRIVATE);
                    intent.putExtra("channel", channelID);
                    intent.putExtra("username", recv.getString("user_name", "unknown"));
                    intent.putExtra("rtcAuthInfo", info);
                    intent.putExtra("targetClientID", targetClientID);
                    intent.putExtra("targetName", name);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    // 判断对方是否在线，如果在线就进入选择是否接听的Activity。
                    new Thread(()->{
                        try {
                            boolean isOnline = MqttByAli.checkIsOnline(targetClientID);
                            if (isOnline) {
                                startActivity(intent);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();

                    break;
                }
                case "CallCancel": {
                    Intent intent = new Intent("CALLCANCEL");
                    intent.setPackage(getPackageName());
                    sendBroadcast(intent);
                    break;
                }
                case "GroupInvite" : {
                    User groupInfo = (User) object.get("groupInfo");
                    // 订阅群聊topic
                    String[] topicFilter = new String[1];
                    int[] qos = new int[1];
                    if (groupInfo == null) break;
                    topicFilter[0] = String.valueOf(groupInfo.getUser_id());
                    qos[0] = groupChatQos;
                    mqtt.subscribe(topicFilter, qos);

                    // 将消息显示在消息界面
                    localMessage.setMsgType(MsgType.GROUP_INVITE);
                    localMessage.setGroup(true);
                    localMessage.setTargetId(String.valueOf(groupInfo.getUser_id()));
                    TextMsgBody msgBody = new TextMsgBody();
                    User targetUser = getUserFromContactData(localMessage.getSenderId());
                    String name = targetUser == null ? "unknown" : targetUser.getUser_name();
                    msgBody.setMessage(name + "邀请你入群");
                    localMessage.setBody(msgBody);
                    sendMessageBroadcast(localMessage);
                    break;
                }
            }


        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            Log.e(TAG, "deliveryComplete  token :" + token );
        }

        // 接收接收文件，并返回File
        private File receiveFile(JSONObject object) {
            int total = object.getInteger("total");
            int order = object.getInteger("order");
            byte[] dataFile = object.getBytes("data");
            Log.e("Service", "(1)  byte[]:" + (dataFile == null) + "order:" + order);
            String filePath = "data/data/" + getApplication().getPackageName() + "/files/"; // 文件存储目录
            File file = null; // 最终文件的对象
            if (total == 1 && order == 0) {
                file = FileUtils.bytesToFile(dataFile, filePath, object.getString("name"));
            } else {
                String hex = object.getString("hex");
                if (order == 0) {
                    HashMap<Integer, byte[]> map = new HashMap<>();
                    Log.e("createNewCache", "(2)  byte[]:" + dataFile +  "  order:" + order + " hex:" + hex + " map:" + map);
                    map.put(order, dataFile);
                    FileCache.createNewCache(hex, map);
                } else {
                    Log.e("add2Cache", "(3)  byte[]:" + dataFile +  "  order:" + order + " hex:" + hex);
                    FileCache.add2Cache(hex, order, dataFile);
                }
                System.out.println(FileCache.getCount(hex) + "//" + total);
                if (FileCache.getCount(hex) == total) { // 接收完成
                    String name = object.getString("name");
                    int length = object.getInteger("length");
                    file = FileCache.mergeToFile(hex, total, length, filePath, name);
                    Log.e(TAG, "create file");
                }
            }
            return file;
        }

        private void sendMessageBroadcast(Message localMessage) {
            // 发出广播(将消息对象放入Intent中)
            Intent msgArriveIntent = new Intent(MESSAGEARRIVEACTION);
            msgArriveIntent.setPackage(getPackageName()); // 只发给本应用的接收器
            msgArriveIntent.putExtra("message", localMessage);
            sendBroadcast(msgArriveIntent);
            Log.e(TAG, "发出广播");
        }

    }

}