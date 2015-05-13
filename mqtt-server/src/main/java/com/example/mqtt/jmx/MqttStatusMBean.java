package com.example.mqtt.jmx;

/**
 * Created by guanxinquan on 15-5-12.
 */
public interface MqttStatusMBean {

    public int getConnectCnt();

    public int getConnectCntByUserId(String userId);

    public long getPubStubCnt();

    public long getQosFlightCnt();
}
