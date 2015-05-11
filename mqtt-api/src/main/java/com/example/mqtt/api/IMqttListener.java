package com.example.mqtt.api;

import com.example.mqtt.event.listener.ListenerEvent;

/**
 * Created by guanxinquan on 15-5-11.
 */
public interface IMqttListener {
    public Object eventArrival(ListenerEvent event);
}
