package com.hrl.chaui.bean;


import java.io.Serializable;

public  class  MsgBody implements Serializable {

    private static final long serialVersionUID = 20210825L;

    private MsgType localMsgType;

    public MsgType getLocalMsgType() {
        return localMsgType;
    }

    public void setLocalMsgType(MsgType localMsgType) {
        this.localMsgType = localMsgType;
    }
}
