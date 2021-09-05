package com.hrl.chaui.bean;

public class UserForParam {
    private String user_name;
    private String user_pwd;
    private Integer user_id;
    private String user_img;
    private String user_gender;
    private String user_phone;
    private String user_sign;
    private byte[] img_data;

    public UserForParam(){
        this.user_name="";
        this.user_pwd="";
        this.user_img="";
        this.user_gender="";
        this.user_phone="";
        this.user_sign="";
        this.img_data=new byte[]{};
    }
    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_pwd() {
        return user_pwd;
    }

    public void setUser_pwd(String user_pwd) {
        this.user_pwd = user_pwd;
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

    public String getUser_gender() {
        return user_gender;
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

    public byte[] getImg_data() {
        return img_data;
    }

    public void setImg_data(byte[] img_data) {
        this.img_data = img_data;
    }

}
