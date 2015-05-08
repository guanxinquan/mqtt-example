package com.example.mqtt.cluster;

import com.example.mqtt.api.MqttListener;
import com.example.mqtt.api.MqttServer;

/**
 * Created by guanxinquan on 15-5-8.
 * 维护mqtt远程调用的集群
 */
public interface IMqttCluster {

    /**
     * 获取mqtt服务器的rpc服务，按照一致性hash算法计算服务器
     * @param userId
     * @return
     */
    public MqttServer getMqttServer(Long userId);

    /**
     * 获取mqtt服务器的地址，按照一致性hash算法计算服务器地址
     * @param userId
     * @return
     */
    public String getMqttServerUrl(Long userId);

    /**
     * 随机获取一个远端服务器
     * @return
     */
    public MqttListener getMqttListener();

}
