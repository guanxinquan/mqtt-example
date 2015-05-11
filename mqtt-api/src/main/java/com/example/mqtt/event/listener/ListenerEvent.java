package com.example.mqtt.event.listener;

import java.io.Serializable;

/**
 * Created by guanxinquan on 15-5-11.
 * mqtt服务监听事件
 *
 */
public abstract class ListenerEvent implements Serializable{

    private String clientID;

    private Long userID;

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }
}
