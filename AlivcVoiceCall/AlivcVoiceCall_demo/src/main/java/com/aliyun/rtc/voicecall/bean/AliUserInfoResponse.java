package com.aliyun.rtc.voicecall.bean;

import java.io.Serializable;
import java.util.List;

public class AliUserInfoResponse implements Serializable {

    private int code;
    private AliUserInfo data;
    private int server;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public AliUserInfo getAliUserInfo() {
        return data;
    }

    public void setAliUserInfo(AliUserInfo data) {
        this.data = data;
    }

    public int getServer() {
        return server;
    }

    public void setServer(int server) {
        this.server = server;
    }

    public static class AliUserInfo implements Serializable {

        private String appid;
        private String userid;
        private String nonce;
        private int timestamp;
        private String token;
        private TurnBean turn;
        private List<String> gslb;

        public String getAppid() {
            return appid;
        }

        public void setAppid(String appid) {
            this.appid = appid;
        }

        public String getUserid() {
            return userid;
        }

        public void setUserid(String userid) {
            this.userid = userid;
        }

        public String getNonce() {
            return nonce;
        }

        public void setNonce(String nonce) {
            this.nonce = nonce;
        }

        public int getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(int timestamp) {
            this.timestamp = timestamp;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public TurnBean getTurn() {
            return turn;
        }

        public void setTurn(TurnBean turn) {
            this.turn = turn;
        }

        public List<String> getGslb() {
            return gslb;
        }

        public void setGslb(List<String> gslb) {
            this.gslb = gslb;
        }

        public static class TurnBean implements Serializable {

            private String username;
            private String password;

            public String getUsername() {
                return username;
            }

            public void setUsername(String username) {
                this.username = username;
            }

            public String getPassword() {
                return password;
            }

            public void setPassword(String password) {
                this.password = password;
            }
        }
    }
}
