package com.hrl.chaui.bean;

import com.mcxtzhang.indexlib.IndexBar.bean.BaseIndexPinyinBean;

public class User extends BaseIndexPinyinBean {
    private boolean isTop;
    private String name;
    private String id;
    private String img;
    private String gender;
    private String phone;
    private String sign;

    public User(){ }

    public User(String name){
        this.name=name;
    }
    public User setTop(boolean top) {
        isTop = top;
        return this;
    }
    public String getName(){
        return name;
    }
    public String getImg(){
        return img;
    }
    public void setName(String name){
        this.name=name;
    }
    @Override
    public String getTarget() {
        return name;
    }

    @Override
    public boolean isNeedToPinyin() {
        return !isTop;
    }

    @Override
    public boolean isShowSuspension() {
        return isTop;
    }
}
