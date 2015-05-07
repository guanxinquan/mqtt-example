package com.example.mqtt.api;

import com.example.mqtt.model.PublishModel;

/**
 * Created by guanxinquan on 15-5-7.
 *
 *
 *
 */
public interface IMqttService {


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
    public void publish(PublishModel message);
}
