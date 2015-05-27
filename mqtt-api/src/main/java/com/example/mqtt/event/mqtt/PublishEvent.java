package com.example.mqtt.event.mqtt;

/**
 * Created by guanxinquan on 15-5-11.
 * mqtt发送事件
 */
public class PublishEvent extends MqttEvent {

    private byte[] payLoad;

    private String topic;

    private String syncTag;

    public PublishEvent() {
    }

    public PublishEvent(String clientId,Long userId,byte[] payLoad, String topic,String syncTag) {
        setClientID(clientId);
        setUserID(userId);
        this.payLoad = payLoad;
        this.topic = topic;
        this.syncTag = syncTag;
    }

    public PublishEvent(Long userId,byte[] payLoad, String topic) {
        setUserID(userId);
        this.payLoad = payLoad;
        this.topic = topic;
    }


    public String getSyncTag() {
        return syncTag;
    }

    public void setSyncTag(String syncTag) {
        this.syncTag = syncTag;
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
