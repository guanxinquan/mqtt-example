package com.example.mqtt.event;

/**
 * Created by guanxinquan on 15-5-7.
 */
public class PublishEvent extends MqttEvent {

    private String payload;

    private String topic;

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
