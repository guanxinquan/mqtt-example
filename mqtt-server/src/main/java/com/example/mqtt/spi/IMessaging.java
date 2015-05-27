package com.example.mqtt.spi;

import com.example.mqtt.jmx.MqttStatusMBean;
import com.example.mqtt.proto.messages.AbstractMessage;
import com.example.mqtt.server.ServerChannel;

import java.io.Closeable;

/**
 * Created by guanxinquan on 15-5-5.
 *
 * 消息处理，从网络来的请求，通过这个接口，调用内部逻辑，处理客户端请求
 *
 *
 */
public interface IMessaging extends MqttStatusMBean,Closeable,IMqttService {

    /**
     * 处理协议消息
     * @param session 客户端链接相关信息
     * @param msg 需要处理的消息
     */
    void handleProtocolMessage(ServerChannel session, AbstractMessage msg);


    /**
     * 从网络下发消息
     * @param clientId 客户id
     * @param content 内容
     * @param topic 主题
     */
    public void sendMessage(String clientId,byte[] content,String topic,String syncTag);

    /**
     * 通过用户名字或者id下发消息
     * @param userId 用户id
     * @param content 消息内容
     * @param topic 主题
     */
    public void sendMessageByUser(String userId,byte[] content,String topic);

    /**
     *
     * 当客户端与服务端断开链接时，调用这个接口，用于清理系统内部维护的mqtt相关的信息
     *
     * @param clientID 客户id
     */
    void lostConnection(String clientID);

    /**
     * dispatcher 服务希望mqtt提供某个用户的最新的版本号
     * @param userId
     */
    void syncDown(Long userId);

}
