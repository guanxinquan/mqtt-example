package com.example.mqtt.listener;

import com.example.mqtt.api.IMqttListener;
import com.example.mqtt.api.IMqttRemoteListener;
import com.example.mqtt.event.listener.PublishEvent;
import com.example.mqtt.mq.IMqListener;
import com.example.mqtt.mq.MqMessageOperator;
import com.example.mqtt.zk.IZkServer;
import com.example.mqtt.zk.ZkServerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Collections;

/**
 * Created by guanxinquan on 15-5-11.
 */
public class RemoteMqttListenerFactory {

    private static IMqttRemoteListener listener ;
    private static boolean isStart = false;
    private static IZkServer zkServer = ZkServerFactory.getInstance();

    private static final String QUEUE_NAME = "mqtt-queue";

    private static final Logger logger = LoggerFactory.getLogger(RemoteMqttListenerFactory.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    public synchronized static void start(final IMqttListener mqttListener){

        if(isStart){
            return;
        }


        try {
            listener = new RemoteMqttListener(mqttListener);
        } catch (RemoteException e) {
            logger.info("create remote listener error",e);
        }

        try {
            MqMessageOperator operator = new MqMessageOperator();
            IMqListener mqListener = new IMqListener() {
                @Override
                public void listener(byte[] data) {
                    try {
                        PublishEvent event  = mapper.readValue(data, PublishEvent.class);
                        mqttListener.eventArrival(event);
                    } catch (IOException e) {
                        logger.error("json parser error",e);
                    }
                }
            };
            operator.addListeners(QUEUE_NAME,Collections.singletonList(mqListener));
        } catch (Exception e) {
            logger.error("add rabbit mq listener error",e);
        }


        String host = System.getProperty("rmiHost");
        String port = System.getProperty("rmiPort");
        if(host == null)
            host = "localhost";
        if(port == null){
            port = "1099";
        }
        String url = String.format("rmi://%s:%d/%s",host,Integer.valueOf(port),IMqttRemoteListener.class.getTypeName());
        logger.info("register rmi service :{}",url);
        try {

            LocateRegistry.createRegistry(Integer.valueOf(port));
            Naming.rebind(url,listener);
            zkServer.registerApiProvider(IMqttRemoteListener.class.getTypeName(),host,Integer.valueOf(port),null);
        } catch (Exception e){
            logger.error("create remote mqtt listener error ",e);
        }

    }

}
