package com.example.mqtt.jmx;

import com.example.mqtt.spi.IMessageFactory;
import com.example.mqtt.spi.IMessaging;

/**
 * Created by guanxinquan on 15-5-12.
 */
public class MqttStatus implements MqttStatusMBean {

    IMessaging messaging = IMessageFactory.getInstance();


    /**
     * 获取当前的链接数
     * @return
     */
    @Override
    public int getConnectCnt() {
        return messaging.getConnectCnt();
    }

    /**
     * 获取用户的连接数
     * @param userId
     * @return
     */
    @Override
    public int getConnectCntByUserId(String userId) {
        return messaging.getConnectCntByUserId(userId);
    }
}
