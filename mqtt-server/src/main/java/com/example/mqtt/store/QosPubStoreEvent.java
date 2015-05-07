package com.example.mqtt.store;

import com.example.mqtt.proto.messages.PublishMessage;

/**
 * Created by guanxinquan on 15-5-7.
 */
public class QosPubStoreEvent {

    private int cnt;

    private PublishMessage message;

    private String clientID;

    private long expireTime;

    private boolean validate = true;

    public QosPubStoreEvent(String clientID, PublishMessage message) {
        this.clientID = clientID;
        this.message = message;
        this.cnt = 0;
    }


    public QosPubStoreEvent() {
    }


    public int getCnt() {
        return cnt;
    }

    public void setCnt(int cnt) {
        this.cnt = cnt;
    }

    public PublishMessage getMessage() {
        return message;
    }

    public void setMessage(PublishMessage message) {
        this.message = message;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public boolean isValidate() {
        return validate;
    }

    public void setValidate(boolean validate) {
        this.validate = validate;
    }
}
