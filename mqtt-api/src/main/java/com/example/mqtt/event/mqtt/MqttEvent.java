package com.example.mqtt.event.mqtt;

import java.io.Serializable;

/**
 * Created by guanxinquan on 15-5-11.
 *
 * 调用mqtt服务的执行事件
 *
 */
public abstract class MqttEvent implements Serializable{

    private Long userID;

    private String clientID;

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }
}
