package com.example.mqtt.listener;

import com.example.mqtt.zk.IZkServer;
import com.example.mqtt.zk.ZkServerFactory;

/**
 * Created by guanxinquan on 15-5-15.
 */
public class IGetMqttHost {

    public static void main(String[] args){
        System.setProperty("zk","123.126.105.45:2182");
        IZkServer zkServer = ZkServerFactory.getInstance();
        System.out.println(zkServer.fetchServer("201212"));
    }
}
