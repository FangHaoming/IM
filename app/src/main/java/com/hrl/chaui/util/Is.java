package com.hrl.chaui.util;

import com.hrl.chaui.bean.User;

import java.util.List;

import static com.hrl.chaui.MyApplication.friendData;

public class Is {
    public static boolean isFriendById(int user_id){
        List<User> users=friendData;
        for(int i=0;i<users.size();i++){
            if(user_id==users.get(i).getUser_id()){
                return true;
            }
        }
        return false;
    }
    public static boolean isFriendByPhone(String user_phone){
        List<User> users=friendData;
        for(int i=0;i<users.size();i++){
            if(user_phone.equals(users.get(i).getUser_phone())){
                return true;
            }
        }
        return false;
    }
    public static int getIdByPhone(String user_phone){
        List<User> users=friendData;
        for(int i=0;i<users.size();i++){
            if(user_phone.equals(users.get(i).getUser_phone())){
                return users.get(i).getUser_id();
            }
        }
        return -1;
    }
}
