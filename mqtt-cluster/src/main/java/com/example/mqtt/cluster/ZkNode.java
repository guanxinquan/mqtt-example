package com.example.mqtt.cluster;

/**
 * Created by guanxinquan on 15-5-7.
 * 表述一个mqtt node在zk上配置信息
 */
public class ZkNode {

    /**
     * mqtt服务器地址
     */
    private String hostName;
    /**
     * mqtt服务器端口号
     */
    private Integer mqttPort;

    /**
     * mqtt服务编号，用于一致性hash
     */
    private Integer seq;

    public ZkNode(String hostName, Integer mqttPort, Integer seq) {
        this.hostName = hostName;
        this.mqttPort = mqttPort;
        this.seq = seq;
    }

    public ZkNode() {
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public Integer getMqttPort() {
        return mqttPort;
    }

    public void setMqttPort(Integer mqttPort) {
        this.mqttPort = mqttPort;
    }

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }
}
