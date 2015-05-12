package com.example.mqtt.listener;

import com.example.mqtt.api.IMqttRemoteListener;
import com.example.mqtt.zk.IZkServer;
import com.example.mqtt.zk.ZkServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

/**
 * Created by guanxinquan on 15-5-11.
 */
public class RemoteMqttListenerFactory {

    private static IMqttRemoteListener listener ;
    private static IZkServer zkServer = ZkServerFactory.getInstance();

    private static final Logger logger = LoggerFactory.getLogger(RemoteMqttListenerFactory.class);


    static {
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
            listener = new RemoteMqttListener();
            LocateRegistry.createRegistry(Integer.valueOf(port));
            Naming.rebind(url,listener);
            zkServer.registerApiProvider(IMqttRemoteListener.class.getTypeName(),host,Integer.valueOf(port),null);
        } catch (Exception e){
            logger.error("create remote mqtt listener error ",e);
        }
    }

    public static final IMqttRemoteListener getRemoteMqttListener(){
        return listener;
    }
}
