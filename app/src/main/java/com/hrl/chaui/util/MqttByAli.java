package com.hrl.chaui.util;


import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.hrl.chaui.bean.User;
import com.hrl.chaui.util.ConnectionOptionWrapper;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

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
                    System.out.println("connect success");
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
        try {
            mqttClient.subscribe(topicFilters, qos);
        } catch (MqttException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }// 订阅主题

    }

    public void sendMessage(String msg) {
        sendMessage(topic, msg);
    }

    //向群里发送信息
    public void sendMessage(String topic, String msg){
        try {
            MqttMessage message = new MqttMessage();
            message.setQos(qosLevel);
            message.setRetained(true);
            message.setPayload(msg.getBytes());
            /**
             * 发送普通消息时，Topic必须和接收方订阅的Topic一致，或者符合通配符匹配规则。
             */
            mqttClient.publish(topic, message);
        } catch (MqttPersistenceException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        } catch (MqttException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
    }

    //向单个用户发送信息，私聊
    public void sendMessageP2P(String msg, String targetClientId) {
        /**
         * 微消息队列MQTT版支持点对点消息，即如果发送方明确知道该消息只需要给特定的一个设备接收，且知道对端的clientId，则可以直接发送点对点消息。
         * 点对点消息不需要经过订阅关系匹配，可以简化订阅方的逻辑。点对点消息的topic格式规范是 {{parentTopic}}/p2p/{{targetClientId}}。
         */
        String p2pTopic = parentTopic + "/p2p/" + targetClientId;
        MqttMessage message = new MqttMessage(msg.getBytes());
        message.setQos(qosLevel);
        message.setRetained(true);
        try {
            mqttClient.publish(p2pTopic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void friendRequest(User user,String targetClientId){
        JSONObject object=(JSONObject)JSONObject.toJSON(user);
        object.put("msg","friendRequest");
        String message=JSONObject.toJSONString(object);
        sendMessageP2P(message,targetClientId);
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
}
