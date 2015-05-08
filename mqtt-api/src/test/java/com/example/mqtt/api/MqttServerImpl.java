package com.example.mqtt.api;

import org.apache.thrift.TException;

/**
 * Created by guanxinquan on 15-5-8.
 */
public class MqttServerImpl implements MqttServer.Iface {

    @Override
    public void publishByUserId(long userId, String payload, String topic) throws TException {
        System.out.println("publish by user id userId "+userId+" payload "+payload+" topic  "+topic);
    }

    @Override
    public void publishByClientId(String clientId, long userId, String payload, String topic) throws TException {
        System.out.println("publish by client id clientId "+clientId + "userId "+userId + " payload "+payload + " topic "+topic);
    }
}
