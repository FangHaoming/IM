package com.hrl.chaui.bean;

public class ContactEntity {
    private String name;
    private String url_img;
    private String stage;
    private String sign;

    public ContactEntity(String name, String url_img, String stage, String sign) {
        this.name = name;
        this.url_img = url_img;
        this.stage = stage;
        this.sign = sign;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl_img() {
        return url_img;
    }

    public void setUrl_img(String url_img) {
        this.url_img = url_img;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
