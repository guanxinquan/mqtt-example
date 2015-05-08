package com.example.mqtt.zk;

/**
 * Created by guanxinquan on 15-5-8.
 */
public class ZkServerFactory {

    private static final String hosts = System.getProperty("zk");

    private static ZkServer instance = new ZkServer(hosts);

    public static final ZkServer getInstance(){
        return instance;
    }

}
