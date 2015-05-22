package com.example.mqtt.mq;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by guanxinquan on 15-5-22.
 */
public class MqListenerTest {

    public static void main(String[] args) throws Exception {
        MqMessageOperator operator = new MqMessageOperator();
        List<IMqListener> list = new ArrayList<IMqListener>();
        list.add(new IMqListener() {
            @Override
            public void listener(byte[] data) {
                System.out.println(new String(data, Charset.forName("utf-8")));
            }
        });
        operator.addListeners("mqtt-queue",list);
    }
}
