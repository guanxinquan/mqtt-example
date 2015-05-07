package com.example.mqtt.event;

/**
 * Created by guanxinquan on 15-5-7.
 * mqtt消息的一个父类。
 */
public class MqttEvent {

    private String clientID;

    private String userName;

    private String numberId;

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNumberId() {
        return numberId;
    }

    public void setNumberId(String numberId) {
        this.numberId = numberId;
    }
}
