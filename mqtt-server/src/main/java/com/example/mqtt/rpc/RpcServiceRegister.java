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

    static {

        String host = System.getProperty("rmiHost");
        String port = System.getProperty("rmiPort");

        if(host == null)
            host= "localhost";
        if(port == null)
            port = "1088";


        String url = String.format("rmi://%s:%d/%s",host,1088,IMqttService.class.getTypeName());
        logger.info("register rmi service : {}",url);

        try {
            IMqttService mqttService = new MqttServerImpl();
            LocateRegistry.createRegistry(1088);
            Naming.rebind(url, mqttService);
            zkServer.registerApiProvider(IMqttService.class.getTypeName(),"localhost",1088,null);
        }catch (Exception e){
            logger.error("register rmi server error ",e);
        }
    }

    public static void register(){
    }


}
