package com.example.mqtt.event.mqtt;

/**
 * Created by guanxinquan on 15-5-11.
 * mqtt踢出事件
 */
public class KickEvent extends MqttEvent {

    public KickEvent(String clientID,Long userID) {
        setClientID(clientID);
        setUserID(userID);
    }

    public KickEvent() {
    }
}
