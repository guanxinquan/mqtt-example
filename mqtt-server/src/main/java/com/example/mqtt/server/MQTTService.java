package com.example.mqtt.server;

import com.example.mqtt.rpc.RpcServiceRegister;
import com.example.mqtt.server.netty.NettyAcceptor;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by guanxinquan on 15-5-6.
 */
public class MQTTService  {

    private MQTTConfig config = new MQTTConfig();

    private ServerAcceptor acceptor;


    public void startServer() throws IOException {
        acceptor = new NettyAcceptor();
        Properties properties = new Properties();
        properties.put("port",1883);

        RpcServiceRegister.register();

        acceptor.initialize(null, properties);
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                System.out.println("Server stopping...");
                acceptor.close();
                System.out.println("Server stopped");
            }
        });

    }

}
