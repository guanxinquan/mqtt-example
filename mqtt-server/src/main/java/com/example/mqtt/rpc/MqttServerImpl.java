package com.example.mqtt.rpc;

import com.example.mqtt.api.MqttServer;
import com.example.mqtt.spi.IMessageFactory;
import com.example.mqtt.spi.IMessaging;
import org.apache.thrift.TException;

/**
 * Created by guanxinquan on 15-5-8.
 *
 * 远程调用服务端的借口
 *
 */
public class MqttServerImpl implements MqttServer.Iface {

    IMessaging messaging = IMessageFactory.getInstance();

    @Override
    public void publishByUserId(long userId, String payload, String topic) throws TException {

    }

    @Override
    public void publishByClientId(String clientId, long userId, String payload, String topic) throws TException {
        messaging.sendMessage(clientId,payload,topic);
    }
}
