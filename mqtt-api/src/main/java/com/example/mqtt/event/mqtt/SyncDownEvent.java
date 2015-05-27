package com.example.mqtt.event.mqtt;

/**
 * Created by guanxinquan on 15-5-27.
 * dispatch 服务调用mqtt服务，触发sync动作
 */
public class SyncDownEvent extends MqttEvent{

    public SyncDownEvent(Long userId) {
        setUserID(userId);
    }
}
