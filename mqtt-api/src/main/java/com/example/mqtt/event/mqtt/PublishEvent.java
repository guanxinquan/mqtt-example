package com.example.mqtt.event.mqtt;

/**
 * Created by guanxinquan on 15-5-11.
 * mqtt发送事件
 */
public class PublishEvent extends MqttEvent {

    private byte[] payLoad;

    private String topic;

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
