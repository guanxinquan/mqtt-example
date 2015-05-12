package com.example.mqtt.event.listener;

/**
 * Created by guanxinquan on 15-5-11.
 * 用户发送事件
 */
public class PublishEvent extends ListenerEvent {

    private byte[] payLoad;

    private String topic;

    public PublishEvent() {
    }

    public PublishEvent(String clientId,Long userId,byte[] payLoad, String topic) {
        setClientID(clientId);
        setUserID(userId);
        this.payLoad = payLoad;
        this.topic = topic;
    }

    public byte[] getPayLoad() {
        return payLoad;
    }

    public void setPayLoad(byte[] payLoad) {
        this.payLoad = payLoad;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
