package com.example.mqtt.event.listener;

/**
 * Created by guanxinquan on 15-5-11.
 * 用户断开连接通知
 *
 *
 */
public class DisconnectEvent extends ListenerEvent {


    public DisconnectEvent(String clientID,Long userID) {
        setClientID(clientID);
        setUserID(userID);
    }

    public DisconnectEvent() {
    }
}
