package com.example.mqtt.spi;

import com.example.mqtt.store.QosPubStoreEvent;
import com.example.mqtt.proto.messages.PublishMessage;

/**
 * Created by guanxinquan on 15-5-7.
 *
 * mqtt服务，是一个可以对外的服务接口，用于服务端业务逻辑，通过rpc的形式调用mqtt相关服务
 *
 */
public interface IMqttService {


    /**
     * 重新发布。根据mqtt协议，如果qos1的消息下发时，等待特定时间后，没有收到ack，那么服务端会从新下发这个消息。
     * @param event 需要下发的消息
     * @return 下发是否成功 如果下发过程中，客户端链接已经断开，说明下发不成功。
     */
    public boolean republish(QosPubStoreEvent event);

    /**
     * 踢出指定clientID的客户端链接
     * @param clientID
     * @return
     */
    public boolean kickOut(String clientID);

    /**
     * 下发消息
     * @param message 消息信息
     */
    public void publish(PublishMessage message);
}
