package com.example.mqtt.cluster;

/**
 * Created by guanxinquan on 15-5-7.
 * 监听事件，当zk上的客户端或者mqtt服务器发生变更时，触发事件
 *
 */
public interface IClusterListener {

    public void mqttServerChange();

    public void mqttRpcClientChange();


}
