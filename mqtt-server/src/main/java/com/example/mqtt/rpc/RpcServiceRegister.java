package com.example.mqtt.rpc;

import com.example.mqtt.api.IMqttService;
import com.example.mqtt.zk.IZkServer;
import com.example.mqtt.zk.ZkServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

/**
 * Created by guanxinquan on 15-5-11.
 */
public class RpcServiceRegister {

    private static final IZkServer zkServer = ZkServerFactory.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(RpcServiceRegister.class);
    public static String port = "1088";

    static {

        String host = System.getProperty("host");
        String port = System.getProperty("rmiPort");


        if(host == null)
            host= "localhost";
        if(port == null)
            port = "1088";


        String url = String.format("rmi://%s:%s/%s",host,port,IMqttService.class.getTypeName());
        logger.info("register rmi service : {}",url);

        try {
            IMqttService mqttService = new MqttServerImpl();
            LocateRegistry.createRegistry(Integer.valueOf(port));
            Naming.rebind(url, mqttService);
            zkServer.registerApiProvider(IMqttService.class.getTypeName(),host,Integer.valueOf(port),null);
        }catch (Exception e){
            logger.error("register rmi server error ",e);
        }

    }

    public static void register(){
    }


}
