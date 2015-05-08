package com.example.mqtt.zk;

import java.nio.charset.Charset;

/**
 * Created by guanxinquan on 15-5-8.
 */
public class ZkServerRegisterTest {

    public static void main(String[] args) throws InterruptedException {
        IZkServer server = ZkServerFactory.getInstance("localhost:2181");
        server.registerServerPath("test","test".getBytes(Charset.forName("utf-8")));
        Thread.sleep(300000);
    }
}
