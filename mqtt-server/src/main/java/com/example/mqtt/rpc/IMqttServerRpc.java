package com.example.mqtt.rpc;

import java.io.Closeable;

/**
 * Created by guanxinquan on 15-5-8.
 */
public interface IMqttServerRpc extends Closeable{

    public void startUp(String registerHosts,String host,Integer port,Integer id) throws Exception;

}
