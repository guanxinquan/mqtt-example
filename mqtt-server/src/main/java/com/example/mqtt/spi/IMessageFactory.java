package com.example.mqtt.spi;

import com.example.mqtt.spi.impl.SimpleMessageImpl;

/**
 * Created by guanxinquan on 15-5-8.
 */
public class IMessageFactory {

    private static final IMessaging instance = new SimpleMessageImpl();

    public static final IMessaging getInstance(){
        return instance;
    }


}
