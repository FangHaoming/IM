package com.hrl.chaui.util;

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
        int user_id = Integer.valueOf(userDriveID);
        int target_id = Integer.valueOf(targetDriveID);
        String channelID = null;
        if (user_id < target_id) {
            channelID = userClientID + targetClientID;
        } else {
            channelID = targetClientID + userClientID;
        }
        return channelID.replaceAll("@","");
    }
}
