package com.example.mqtt.cluster;


import java.util.List;

/**
 *
 * mqttCluster 用于将mqtt服务部署为集群服务。mqtt集群服务包括客户端链接一致性hash，mqtt服务调用远程处理服务，远程处理服务调用mqtt服务
 *
 * Created by guanxinquan on 15-5-7.
 */
public interface IMqttCluster {

    /**
     * 注册mqtt服务到zk上
     * @param host
     * @param port
     * @param seq
     */
    public void registerMqttServer(String host,Integer port,Integer seq);

    /**
     * 获取mqtt服务器列表，服务器列表按照seq排序
     * @return
     */
    public List<ZkNode> fetchMqttServers();


    /**
     * 注册mqtt服务器的客户端，即远程服务端
     * @param host
     * @param port
     */
    public void registerRpcClient(String host,Integer port);

    /**
     * 获取mqtt的远程服务端。
     * @return
     */
    public List<ZkNode> fetchRpcClient();


}
