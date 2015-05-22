package com.example.mqtt.mq;

/**
 * Created by guanxinquan on 15-5-22.
 */
public interface IMqListener {
    public void listener(byte[] data);
}
