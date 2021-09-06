package com.hrl.chaui;

import android.app.Application;

import com.hrl.chaui.bean.User;

import java.util.ArrayList;
import java.util.List;

public class MyApplication extends Application {
    public static Application		mApplication;
    public static User modifyUser=new User();
    public static List<User> contactData=new ArrayList<>();
    public static List<User> groupData=new ArrayList<>();
    public static List<User> friendData=new ArrayList<>();
    public static List<User> groupMemberData=new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication=this;
    }

    public static User getUserFromContactData(int user_id) {
        for (User s : contactData) {
            // 注意在通讯录中找好友的时候，通讯录中会有两个另类的User: "群聊"和"新的朋友" 这两位很多属性都是空的。
            if (s.getUser_id() != null && s.getUser_id() == user_id) {
                return s;
            }
        }
        return null;
    }

    /**
     *
     * @param targetClientID : 格式 GID_test@@@{{DriveID}}
     * @return
     */
    public static User getUserFromContactData(String targetClientID) {
        int target_id = -1;

        String[] idInfo = targetClientID.split("@@@");
        if (idInfo.length == 2) {
            try {
                target_id = Integer.parseInt(idInfo[1]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        if (target_id != -1) {
            return getUserFromContactData(target_id);
        }
        return null;
    }

    public static User getGroupFromGroupData(int group_id) {
        for (User s : groupData) {
            if (s.getUser_id() != null && s.getUser_id() == group_id) {
                return s;
            }
        }
        return null;
    }

}
