package com.example.mqtt.api;

import com.example.mqtt.event.MqttEvent;

/**
 * Created by guanxinquan on 15-5-7.
 * 当mqtt服务有消息产生后，通知服务端接收并处理消息。
 */
public interface IMqttListener {

    /**
     * 通知服务端接收处理消息
     * @param event
     */
    public boolean mqttEventArrival(MqttEvent event);
}
