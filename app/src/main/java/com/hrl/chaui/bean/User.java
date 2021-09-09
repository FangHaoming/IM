package com.hrl.chaui.bean;

import com.mcxtzhang.indexlib.IndexBar.bean.BaseIndexPinyinBean;

import java.io.Serializable;

public class User extends BaseIndexPinyinBean implements Serializable {
    private boolean isTop;
    private String user_name;
    private String user_pwd;
    private Integer user_id;
    private String user_img;
    private String user_gender;
    private String user_phone;
    private String user_sign;
    private byte[] img_data;
    private String user_note;
    private int type;
    private int rank;
    private int notice_rank;
    private String nickname;
    private String check;
    private boolean isSelect;

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public String getCheck() {
        return check;
    }

    public void setCheck(String check) {
        this.check = check;
    }

    public String getUser_pwd() {
        return user_pwd;
    }

    public void setUser_pwd(String user_pwd) {
        this.user_pwd = user_pwd;
    }


    public byte[] getImg_data() {
        return img_data;
    }

    public void setImg_data(byte[] img_data) {
        this.img_data = img_data;
    }



    public User(){ }

    public User(String name,String phone){
        this.user_name=name;
        this.user_phone=phone;
    }

    public User(Integer id,String img,String name,String phone,Integer type){
        this.user_id=id;
        this.user_img=img;
        this.user_name=name;
        this.type=type;
        this.user_phone=phone;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public int getType(){
        return type;
    }


    public boolean isTop(){
        return isTop;
    }

    public User setTop(boolean top) {
        isTop = top;
        return this;
    }



    @Override
    public String getTarget() {
        return user_name;
    }

    @Override
    public boolean isNeedToPinyin() {
        return !isTop;
    }

    @Override
    public boolean isShowSuspension() {
        return !isTop;
    }


    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public String getUser_img() {
        return user_img;
    }

    public void setUser_img(String user_img) {
        this.user_img = user_img;
    }

    public String getUser_note() {
        return user_note;
    }

    public void setUser_note(String user_note) {
        this.user_note = user_note;
    }

    public String getUser_gender() {
        return user_gender==null?"?":user_gender;
    }

    public void setUser_gender(String user_gender) {
        this.user_gender = user_gender;
    }

    public String getUser_phone() {
        return user_phone;
    }

    public void setUser_phone(String user_phone) {
        this.user_phone = user_phone;
    }

    public String getUser_sign() {
        return user_sign;
    }

    public void setUser_sign(String user_sign) {
        this.user_sign = user_sign;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getNotice_rank() {
        return notice_rank;
    }

    public void setNotice_rank(int notice_rank) {
        this.notice_rank = notice_rank;
    }


}
