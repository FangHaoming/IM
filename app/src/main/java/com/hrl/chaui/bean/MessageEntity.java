package com.hrl.chaui.bean;

public class MessageEntity {

    private String name;
    private String url_img;
    private String time;
    private String messages;

    public MessageEntity(String name, String url_img, String time, String messages) {
        this.name = name;
        this.url_img = url_img;
        this.time = time;
        this.messages = messages;
    }

    public String getName() {
        return name;
    }

    public String getUrl_img() {
        return url_img;
    }

    public String getTime() {
        return time;
    }

    public String getMessages() {
        return messages;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl_img(String url_img) {
        this.url_img = url_img;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setMessages(String messages) {
        this.messages = messages;
    }
}
