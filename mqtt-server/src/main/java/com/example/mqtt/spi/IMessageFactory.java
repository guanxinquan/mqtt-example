package com.example.mqtt.spi;

import com.example.mqtt.spi.impl.SimpleMessageImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by guanxinquan on 15-5-8.
 */
public class IMessageFactory {

    private static final Logger logger = LoggerFactory.getLogger(IMessageFactory.class);

    private static IMessaging instance = new SimpleMessageImpl();

    static {
        Runtime.getRuntime().addShutdownHook(new ShutDown());
    }

    public static final IMessaging getInstance(){
        return instance;
    }

    static class ShutDown extends Thread{
        @Override
        public void run() {
            try {
                if(instance != null) {
                    logger.info("shut down messaging ....");
                    instance.close();
                    instance = null;
                }
            } catch (IOException e) {
                logger.error("close message error ",e);
            }
        }
    }

}
