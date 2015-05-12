package com.example.mqtt.zk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by guanxinquan on 15-5-8.
 *
 * zk服务的工厂类
 *
 *
 */
public class ZkServerFactory {

    private static final String hosts = System.getProperty("zk");

    private static final Logger logger = LoggerFactory.getLogger(ZkServerFactory.class);

    private static ZkServer instance ;

    static {
        try {
            instance = new ZkServer(hosts);
            Runtime.getRuntime().addShutdownHook(new Shutdown());
        }catch (Exception e){
            logger.info("system properties -Dzk should not null or empty");
            System.exit(1);
        }
    }

    /**
     * 获取zk实例
     * @return
     */
    public static final ZkServer getInstance(){
        return instance;
    }

    /**
     * 将zk实例设置为空
     */
    static final void cleanInstance(){
        instance = null;
    }

}


class Shutdown extends Thread{

    private static final Logger logger = LoggerFactory.getLogger(ZkServerFactory.class);

    /**
     * 清理关闭zk服务
     */
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
        ZkServerFactory.cleanInstance();
    }
}
