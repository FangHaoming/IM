package com.hrl.chaui.bean;

import com.mcxtzhang.indexlib.IndexBar.bean.BaseIndexPinyinBean;

public class User extends BaseIndexPinyinBean {
    private boolean isTop;
    private String name;
    private Integer id;
    private String img;
    private String note;
    private String gender;
    private String phone;
    private String sign;
    private int type;

    public String getNote() {
        return note;
    }

    public String getGender() {
        return gender;
    }

    public String getPhone() {
        return phone;
    }

    public String getSign() {
        return sign;
    }



    public User(){ }

    public User(String name){
        this.name=name;
    }

    public User(Integer id,String img,String name,Integer type){
        this.id=id;
        this.img=img;
        this.name=name;
        this.type=type;
    }
    public int getType(){
        return type;
    }
    public Integer getId(){
        return id;
    }

    public boolean isTop(){
        return isTop;
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

    public void setId(Integer id) {
        this.id = id;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public void setType(Integer type) {
        this.type = type;
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
        return !isTop;
    }
}
