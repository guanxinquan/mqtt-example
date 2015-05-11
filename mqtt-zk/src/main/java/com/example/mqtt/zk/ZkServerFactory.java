package com.example.mqtt.zk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by guanxinquan on 15-5-8.
 */
public class ZkServerFactory {

    private static final String hosts = System.getProperty("zk");

    private static ZkServer instance = new ZkServer(hosts);

    static {
        Runtime.getRuntime().addShutdownHook(new Shutdown());
    }

    public static final ZkServer getInstance(){
        return instance;
    }

}

class Shutdown extends Thread{

    private static final Logger logger = LoggerFactory.getLogger(ZkServerFactory.class);
    @Override
    public void run() {
        ZkServer instance = ZkServerFactory.getInstance();
        logger.info("shutdown hook ,disconnect from zookeeper");
        if(instance != null){
            try {
                instance.close();
            } catch (IOException e) {
                logger.error("close zk service error ",e);
            }
        }
    }
}
