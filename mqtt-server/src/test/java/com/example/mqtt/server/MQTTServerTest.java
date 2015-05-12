package com.example.mqtt.server;

import java.io.IOException;

/**
 * Created by guanxinquan on 15-5-7.
 */
public class MQTTServerTest {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.setProperty("zk","localhost:2181");

        MQTTService service = new MQTTService();
        service.startServer();
    }

}
