package com.example.mqtt.server;

import com.example.mqtt.spi.IMessaging;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by guanxinquan on 15-5-5.
 */
public interface ServerAcceptor {

   void initialize(IMessaging messaging, Properties props) throws IOException;

    void close();
}
