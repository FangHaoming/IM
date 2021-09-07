package com.hrl.chaui.util;


import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.onsmqtt20200420.models.QuerySessionByClientIdRequest;
import com.aliyun.onsmqtt20200420.models.QuerySessionByClientIdResponse;
import com.aliyun.teaopenapi.models.Config;
import com.hrl.chaui.bean.User;

import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.File;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MqttByAli {
    /**
     * 您在控制台创建的微消息队列MQTT的实例ID。
     */
    private static final String instanceId = "post-cn-7pp2a3eh70c";
    /**
     * 设置接入点，进入微消息队列MQTT版控制台实例详情页面获取。
     */
    private static final String endPoint = "post-cn-7pp2a3eh70c.mqtt.aliyuncs.com";
    /**
     * AccessKey ID，阿里云身份验证，在阿里云RAM控制台创建。
     */
    private static final String accessKey = value.Access_key;
    /**
     * AccessKey Secret，阿里云身份验证，在阿里云RAM控制台创建。仅在签名鉴权模式下需要设置。
     */
    private static final String secretKey = value.Secret_key;
    /**
     * MQTT客户端ID，由业务系统分配，需要保证每个TCP连接都不一样，保证全局唯一，如果不同的客户端对象（TCP连接）使用了相同的clientId会导致连接异常断开。
     * clientId由两部分组成，格式为GroupID@@@DeviceID，其中GroupID在微消息队列MQTT版控制台创建，DeviceID由业务方自己设置，clientId总长度不得超过64个字符。
     */
    private String clientId = "GID_test@@@1234";
    /**
     * 微消息队列MQTT版消息的一级Topic，需要在控制台创建才能使用。
     * 如果使用了没有创建或者没有被授权的Topic会导致鉴权失败，服务端会断开客户端连接。
     */
    private String parentTopic = "testtopic";
    //子级Topic
    private String subTopic = "";
    /**
     * 微消息队列MQTT版支持子级Topic，用来做自定义的过滤，此处为示例，可以填写任意字符串。
     * 需要注意的是，完整的Topic长度不得超过128个字符。
     */
    private String topic = parentTopic + subTopic;
    /**
     * QoS参数代表传输质量，可选0，1，2。详细信息，请参见名词解释。
     */
    private final int qosLevel = 1;
    //mqtt客户端
    private MqttClient mqttClient;

    //用默认id连接
    public MqttByAli() throws NoSuchAlgorithmException, InvalidKeyException, MqttException {
        this("GID_test@@@1234", "testtopic", null);
    }

    //指定客户端id连接服务端
    public MqttByAli(String client, String pTopic, MqttCallbackExtended callback) throws InvalidKeyException, NoSuchAlgorithmException, MqttException {
        clientId = client;
        parentTopic = pTopic;
        connect(callback);
    }

    public void connect(MqttCallbackExtended callback) throws MqttException, InvalidKeyException, NoSuchAlgorithmException {
        ConnectionOptionWrapper connectionOptionWrapper = new ConnectionOptionWrapper(instanceId, accessKey, secretKey, clientId);
        MemoryPersistence memoryPersistence = new MemoryPersistence();
        /**
         * 客户端协议和端口。客户端使用的协议和端口必须匹配，如果是SSL加密则设置ssl://endpoint:8883。
         */
        mqttClient = new MqttClient("tcp://" + endPoint + ":1883", clientId, memoryPersistence);
        /**
         * 设置客户端发送超时时间，防止无限阻塞。
         */
        mqttClient.setTimeToWait(5000);
        if (callback == null)
            mqttClient.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    Log.e("log", serverURI);
                    Log.e("log","connect success");
                }

                @Override
                public void connectionLost(Throwable throwable) {
                    throwable.printStackTrace();
                }

                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                    /**
                     * 消费消息的回调接口，需要确保该接口不抛异常，该接口运行返回即代表消息消费成功。
                     * 消费消息需要保证在规定时间内完成，如果消费耗时超过服务端约定的超时时间，对于可靠传输的模式，服务端可能会重试推送，业务需要做好幂等去重处理。
                     */
                    System.out.println("receive msg from topic " + s +
                            " , body is " + new String(mqttMessage.getPayload()));
                    Log.e("log","receive msg from topic " + s +
                            " , body is " + new String(mqttMessage.getPayload()));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                    System.out.println("send msg succeed topic is : " + iMqttDeliveryToken.getTopics()[0]);
                    Log.e("log","send msg succeed topic is : " + iMqttDeliveryToken.getTopics()[0]);
                }
            });
        else mqttClient.setCallback(callback);
        MqttConnectOptions options = connectionOptionWrapper.getMqttConnectOptions();
        options.setCleanSession(false);
        options.setAutomaticReconnect(true);
        mqttClient.connect(options);
    }

    //订阅主题，即群聊
    public void subscribe(String[] topicFilters, int[] qos) {

        /**
         *  群聊topic格式： {{parentTopic}}/groupChat/{{groupID}}
         *  这里topicFilters只需要传进来groupID就可以了。
         */
        try {
            for (int i = 0; i < topicFilters.length; i++) {
                topicFilters[i] = parentTopic + "/groupChat/"+topicFilters[i];
            }
            mqttClient.subscribe(topicFilters, qos);
        } catch (MqttException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }// 订阅主题

    }

    /**
     * 向topic发送消息msg
     * @param topic 目标的topic  格式规范： {{parentToppic}}/.../... (子topic可任意)
     * @param msg 要传输的消息
     */
    public void sendMessage(String topic, String msg) {
        MqttMessage message = new MqttMessage(msg.getBytes());
        message.setQos(qosLevel);
        message.setRetained(true);
        try {
            mqttClient.publish(topic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 点到点传输消息msg，目标是targetClientId
     * @param msg :要传输的消息
     * @param targetClientId ：目标clientID    格式规范：{{GroupID}}@@@{{DriveID}}
     */
    public void sendMessageP2P(String msg, String targetClientId) {
        /**
         * 微消息队列MQTT版支持点对点消息，即如果发送方明确知道该消息只需要给特定的一个设备接收，且知道对端的clientId，则可以直接发送点对点消息。
         * 点对点消息不需要经过订阅关系匹配，可以简化订阅方的逻辑。点对点消息的topic格式规范是 {{parentTopic}}/p2p/{{targetClientId}}。
         */
        String p2pTopic = parentTopic + "/p2p/" + targetClientId;
        sendMessage(p2pTopic, msg);
    }


    /**
     * 将data数组中的数据发向targetToppic, 其中jsonAttr是需要附带的消息 (如果数组大于60KB会划分传送)
     * 传输的json数据中，该方法放入了一下字段：length,sendTime,senderID,total,order,hex,data。  还有jsonAttr中的字段。
     * @param data
     * @param targetToppic : 目标最终的toppic  （可以是群聊topic，也可以是客户端topic）    格式规范：{{parentToppic}}/.../... (子topic可任意)
     * @param jsonAttr ： 可以随意加入需要附带的消息。
     */
    public void sendByte(byte[] data, String targetToppic ,Map<String, Object> jsonAttr) {
        JSONObject object=new JSONObject();

        object.put("length",data.length);
        object.put("sendTime", System.currentTimeMillis());
        object.put("senderID", clientId);

        for (String key : jsonAttr.keySet()) {
            object.put(key, jsonAttr.get(key));
        }

        // 设置 hex、total、order、data.
       if (data.length <= 60*1024) {
            object.put("total",1);
            object.put("order",0);
            object.put("hex", DigestUtils.md5Hex(data));
            object.put("data",data);
            sendMessage(targetToppic, object.toJSONString());
        } else {
            long totalLen= data.length;
            String hex =clientId+System.currentTimeMillis();
            object.put("hex", hex);
            int divide=(int)( totalLen%61440==0? totalLen/61440:totalLen/61440+1);
            object.put("total",divide);
            new Thread(()->{
                for (int i=0;i<divide;i++){
                    object.put("order",i);
                    object.put("data", Arrays.copyOfRange(data,i*61440,(int)(i==divide-1?data.length:(i+1)*61440)));
                    sendMessage(targetToppic, object.toJSONString());
                }
            }).start();
        }
    }

    /**
     * 将data数组中的数据发向 clientID 为 targetClientID 的用户。  （在没有附加的消息时，该方法功能大致等于sendMessageP2P，只是该方法会在文件大于60KB时划分传送）
     * @param data
     * @param targetClientID
     * @param jsonAttr ： 需要附带的消息。尽量携带msg字段（接收时会用来辨别消息类别）。其他可按情况随意加入
     */
    public void sendByteP2P(byte[] data, String targetClientID, Map<String, Object> jsonAttr) {
        String p2pTopic = parentTopic + "/p2p/" + targetClientID;
        sendByte(data, p2pTopic, jsonAttr);
    }

    /**
     * 将文本 text 发向 clientID 为targetClient 的用户。
     * @param text
     * @param targetClient ：目标的clientID     规范格式：{{GroupID}}@@@{{DriveID}}
     */
    public void sendTextP2P(String text, String targetClient) {
        byte[] data = null;
        if (text.length() >= 0 && text.getBytes().length <= 60 * 1024) { // 在0~60kb之间
            data = text.getBytes();
        } else {// 截断
            text = text.substring(0, 60 * 1024-1);
            data = text.getBytes();
        }
        HashMap<String, Object> jsonAttr = new HashMap<>();
        jsonAttr.put("msg", "Text");
        sendByteP2P(data, targetClient, jsonAttr);
    }

    // 发送图片文件
    public void sendImageP2P(File file, String targetClientId) {
        HashMap<String, Object> jsonAttr = new HashMap<>();
        jsonAttr.put("msg","Image");
        jsonAttr.put("name",file.getName());

        byte[] data = FileUtils.fileToBytes(file);
        sendByteP2P(data, targetClientId, jsonAttr);
    }

    //私聊发送文件
    public void sendFileP2P(File file, String targetClientId) {
        HashMap<String, Object> jsonAttr = new HashMap<>();
        jsonAttr.put("msg","File");
        jsonAttr.put("name",file.getName());

        byte[] data = FileUtils.fileToBytes(file);
        sendByteP2P(data, targetClientId, jsonAttr);
    }

    //私聊发送视频
    public void sendVideoP2P(File file, String targetClientId) {
        HashMap<String, Object> jsonAttr = new HashMap<>();
        jsonAttr.put("msg","Video");
        jsonAttr.put("name",file.getName());

        byte[] data = FileUtils.fileToBytes(file);
        sendByteP2P(data, targetClientId, jsonAttr);
    }

    //私聊发送语音
    public void sendAudioP2P(File file, String targetClientId, int time) {
        HashMap<String, Object> jsonAttr = new HashMap<>();
        jsonAttr.put("msg","Audio");
        jsonAttr.put("name",file.getName());
        jsonAttr.put("time", time);
        byte[] data = FileUtils.fileToBytes(file);
        sendByteP2P(data, targetClientId, jsonAttr);
    }


    //向群里发送信息
    public void sendByteToGroup(byte[] data, String groupID, Map<String, Object> jsonAttr) {
        // 获取最终的group Topic
        String groupTopic = parentTopic + "/groupChat/" + groupID;
        sendByte(data, groupTopic, jsonAttr);
    }

    // 群聊发送文本
    public void sendTextToGroup(String text, String groupID) {
        byte[] data = null;
        if (text.getBytes().length <= 60 * 1024) { // 在0~60kb之间
            data = text.getBytes();
        } else {// 截断
            text = text.substring(0, 60 * 1024-1);
            data = text.getBytes();
        }
        HashMap<String, Object> jsonAttr = new HashMap<>();
        jsonAttr.put("msg", "Text");
        sendByteToGroup(data, groupID, jsonAttr);
    }

    // 群聊发送图片
    public void sendImageToGroup(File file, String groupID) {
        HashMap<String, Object> jsonAttr = new HashMap<>();
        jsonAttr.put("msg","Image");
        jsonAttr.put("name",file.getName());

        byte[] data = FileUtils.fileToBytes(file);
        sendByteToGroup(data, groupID, jsonAttr);
    }

    // 群聊发送文件
    public void sendFileToGroup(File file, String groupID) {
        HashMap<String, Object> jsonAttr = new HashMap<>();
        jsonAttr.put("msg","File");
        jsonAttr.put("name",file.getName());

        byte[] data = FileUtils.fileToBytes(file);
        sendByteToGroup(data, groupID, jsonAttr);
    }

    // 群聊发送语音
    public void sendAudioToGroup(File file, String groupID, int time) {
        HashMap<String, Object> jsonAttr = new HashMap<>();
        jsonAttr.put("msg","Audio");
        jsonAttr.put("name",file.getName());
        jsonAttr.put("time", time);
        byte[] data = FileUtils.fileToBytes(file);
        sendByteToGroup(data, groupID, jsonAttr);
    }

    // 群聊发送视频
    public void sendVideoToGroup(File file, String groupID) {
        HashMap<String, Object> jsonAttr = new HashMap<>();
        jsonAttr.put("msg","Video");
        jsonAttr.put("name",file.getName());

        byte[] data = FileUtils.fileToBytes(file);
        sendByteToGroup(data, groupID, jsonAttr);
    }

    // 发送P2P语音请求
    public void sendP2PVoiceCallRequest(String targetClientID) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("msg", "VoiceCall");
        byte[] data = new byte[0];
        sendByteP2P(data, targetClientID, map);
    }

    // 发送P2P视频通话请求
    public void sendP2PVideoCallRequest(String targetClientID) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("msg", "VideoCall");
        byte[] data = new byte[0];
        sendByteP2P(data, targetClientID, map);
    }

    // 发送决绝通话请求
    public void sendP2PCallRequestCancel(String targetClientID) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("msg", "CallCancel");
        byte[] data = new byte[0];
        sendByteP2P(data, targetClientID, map);
    }

    // 发送邀请入群的消息
    public void sendP2PGroupInvite(String targetClient, User groupInfo) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("msg", "GroupInvite");
        map.put("groupInfo", groupInfo);
        byte[] data = new byte[0];
        sendByteP2P(data, targetClient, map);
    }


    // 该方法会调用网络请求，必须放在子线程中
    // 该方法返回clientID所代表的用户是否在线
    public static boolean checkIsOnline(String clientID) throws Exception {
        com.aliyun.onsmqtt20200420.Client client = createClient(value.Access_key, value.Secret_key);
        QuerySessionByClientIdRequest querySessionByClientIdRequest = new QuerySessionByClientIdRequest()
                .setClientId(clientID)
                .setInstanceId("post-cn-7pp2a3eh70c");
        // 复制代码运行请自行打印 API 的返回值
        QuerySessionByClientIdResponse response = client.querySessionByClientId(querySessionByClientIdRequest);
        boolean isOnline = response.body.onlineStatus;
        return isOnline;
    }

    public static com.aliyun.onsmqtt20200420.Client createClient(String accessKeyId, String accessKeySecret) throws Exception {
        Config config = new Config()
                // 您的AccessKey ID
                .setAccessKeyId(accessKeyId)
                // 您的AccessKey Secret
                .setAccessKeySecret(accessKeySecret);
        // 访问的域名
        config.endpoint = "onsmqtt.cn-shenzhen.aliyuncs.com";
        return new com.aliyun.onsmqtt20200420.Client(config);
    }

    //发送好友申请
    public void friendRequest(User user,String targetClientId){
        JSONObject object=(JSONObject)JSONObject.toJSON(user);
        HashMap<String,Object> map=new HashMap<>();
        map.put("msg","friendRequest");
        String message=JSONObject.toJSONString(object);
        sendByteP2P(message.getBytes(),targetClientId,map);
    }

    public static String getInstanceId() {
        return instanceId;
    }

    public static String getEndPoint() {
        return endPoint;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getParentTopic() {
        return parentTopic;
    }

    public void setParentTopic(String parentTopic) {
        this.parentTopic = parentTopic;
        this.topic=parentTopic+subTopic;
    }

    public String getSubTopic() {
        return subTopic;
    }

    public void setSubTopic(String subTopic) {
        this.subTopic = subTopic;
        this.topic=parentTopic+subTopic;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getQosLevel() {
        return qosLevel;
    }

    public void disconnect() throws MqttException {
        if(mqttClient!=null) {
            mqttClient.disconnect();
            mqttClient.close();
            mqttClient=null;
        }
    }
}
