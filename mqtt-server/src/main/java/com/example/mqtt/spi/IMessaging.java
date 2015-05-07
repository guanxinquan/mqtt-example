package com.example.mqtt.spi;

import com.example.mqtt.proto.messages.AbstractMessage;
import com.example.mqtt.server.ServerChannel;

/**
 * Created by guanxinquan on 15-5-5.
 *
 * 消息处理，从网络来的请求，通过这个接口，调用内部逻辑，处理客户端请求
 *
 *
 */
public interface IMessaging {

    /**
     * 处理协议消息
     * @param session 客户端链接相关信息
     * @param msg 需要处理的消息
     */
    void handleProtocolMessage(ServerChannel session, AbstractMessage msg);

    /**
     *
     * 当客户端与服务端断开链接时，调用这个接口，用于清理系统内部维护的mqtt相关的信息
     *
     * @param clientID 客户id
     */
    void lostConnection(String clientID);


}
