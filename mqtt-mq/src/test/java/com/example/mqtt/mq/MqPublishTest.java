package com.example.mqtt.mq;

import java.nio.charset.Charset;

/**
 * Created by guanxinquan on 15-5-22.
 */
public class MqPublishTest {

    public static void main(String[] args) throws Exception {
        MqMessageOperator op = new MqMessageOperator();
        while(true) {
            op.publish("mqtt-pub", "mqtt-queue", null, "hello world".getBytes(Charset.forName("utf-8")));
            Thread.sleep(5000l);
        }
    }
}
