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
    public static List<User> groupMemberData=new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication=this;
    }
}
