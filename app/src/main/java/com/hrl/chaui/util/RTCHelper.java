package com.hrl.chaui.util;

import com.aliyun.apsaravideo.sophon.bean.RTCAuthInfo;
import com.aliyun.apsaravideo.sophon.utils.MockAliRtcAuthInfo;
import com.aliyun.rtc.voicecall.bean.AliUserInfoResponse;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

import static com.hrl.chaui.util.value.RTC_AppID;
import static com.hrl.chaui.util.value.RTC_AppKey;
import static com.hrl.chaui.util.value.RTC_Server;

public class RTCHelper {


    /**
     * 通过 登录用户userClientID 和 聊天对象 targetUserID 来获取唯一的 channelID
     * @param userClientID
     * @param targetClientID
     * @return 又两个clientID组成的channelID
     */
    public static String getChannelID(String userClientID, String targetClientID) {
        // 通过ClientID获取DriveID, 然后DriveID小ClientID在前面，DriveID大的ClientID拼接在后面
        String userDriveID = userClientID.split("@@@")[1];
        String targetDriveID = targetClientID.split("@@@")[1];
        int user_id = Integer.parseInt(userDriveID);
        int target_id = Integer.parseInt(targetDriveID);
        String channelID = null;
        if (user_id < target_id) {
            channelID = userClientID + targetClientID;
        } else {
            channelID = targetClientID + userClientID;
        }
        return channelID.replaceAll("@","");
    }

    public static String getNumsChannelID(String userClientID, String targetClientID) {
        String userDriveID = userClientID.split("@@@")[1];
        String targetDriveID = targetClientID.split("@@@")[1];
        int user_id = Integer.valueOf(userDriveID);
        int target_id = Integer.valueOf(targetDriveID);
        String channelID = null;
        if (user_id < target_id) {
            channelID = userDriveID + "000" + targetDriveID;
        } else {
            channelID = targetDriveID + "000" + userDriveID;
        }
        return channelID;
    }


    public static String createToken(
            String appId, String appKey, String channelId, String userId,
            String nonce, Long timestamp
    ) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(appId.getBytes());
        digest.update(appKey.getBytes());
        digest.update(channelId.getBytes());
        digest.update(userId.getBytes());
        digest.update(nonce.getBytes());
        digest.update(Long.toString(timestamp).getBytes());

        String token = DatatypeConverter.printHexBinary(digest.digest()).toLowerCase();
        return token;
    }



    /**
     * 需要跳转到AliRtcChatActivity需要传入Intent一个AliUserInfoResponse.AliUserInfo对象。
     * 该函数可以传入登录用户ClientID和channelID后，得到一个AliUserInfoResponse.AliUserInfo对象。
     * @param channelID
     * @param userID
     * @return
     */
    public static AliUserInfoResponse.AliUserInfo getAliUserInfo(String channelID, String userID) {

        // 注意RTC AliRtcAuthInfo授权信息中的channelID 和userID 只允许数字，字母，下划线_，短划线-
        // 因此这里的UserClientID需要处理一下。
        userID = userID.replaceAll("@@@","");

        AliUserInfoResponse.AliUserInfo  aliUserInfo = new AliUserInfoResponse.AliUserInfo();

        String appKey = RTC_AppKey;
        String appID = RTC_AppID;
        String nonce = String.format("AK-%s", UUID.randomUUID().toString());
        ArrayList<String> list = new ArrayList<>();
        list.add(RTC_Server);
        long timestamp = System.currentTimeMillis()/1000 + 604800;
        String token = null;
        try {
            token = RTCHelper.createToken(appID,appKey,channelID,userID,nonce,timestamp);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        aliUserInfo.setAppid(appID);
        aliUserInfo.setUserid(userID);
        aliUserInfo.setNonce(nonce);
        aliUserInfo.setTimestamp((int)timestamp);
        aliUserInfo.setToken(token);
        aliUserInfo.setTurn(null);
        aliUserInfo.setGslb(list);
        return aliUserInfo;
    }

    public static RTCAuthInfo getVideoCallRTCAuthInfo(String channelID, String userID) {

        // 注意RTC AliRtcAuthInfo授权信息中的channelID 和userID 只允许数字，字母，下划线_，短划线-
        // 因此这里的UserClientID需要处理一下。
        userID = userID.replaceAll("@@@","");

        Calendar nowTime = Calendar.getInstance();
        nowTime.add(Calendar.HOUR_OF_DAY, 48);
        long timestamp = nowTime.getTimeInMillis() / 1000;
        String appKey = RTC_AppKey;

        RTCAuthInfo info = new RTCAuthInfo();
        RTCAuthInfo.RTCAuthInfo_Data info_data = new RTCAuthInfo.RTCAuthInfo_Data();
        info.data = info_data;
        info.data.appid = RTC_AppID;
        info.data.userid = userID;
        info.data.nonce = String.format("AK-%s", UUID.randomUUID().toString());
        info.data.timestamp = timestamp;
        String[] gslb = new String[]{RTC_Server};
        info.data.gslb = gslb;
        info.data.setConferenceId(channelID);

        String token = null;
        try {
            token = RTCHelper.createToken(info.data.appid,appKey,channelID,userID,info.data.nonce,timestamp);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        info.data.token = token;

        return info;
    }

}
