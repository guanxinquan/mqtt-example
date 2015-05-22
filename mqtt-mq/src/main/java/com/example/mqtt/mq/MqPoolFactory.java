package com.example.mqtt.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by guanxinquan on 15-5-22.
 * 由于rabbitmq的Channel不是线程安全的，尽管在我们的应用中是线程安全的，但是，不能够让所有的发送消息都使用
 * 同一个Channel，这样会造成线程同步的阻塞，但是，我们又不能保证为每一个线程分配一个Channel，因此，会使用apache commons pool
 * 为channel做一个pool。如果有必要的情况下，还可以使用多个connection，当前只使用唯一的connection。
 */
public class MqPoolFactory extends BasePooledObjectFactory<Channel>{


    private static final ConnectionFactory factory = new ConnectionFactory();

    protected static final Logger logger = LoggerFactory.getLogger(MqPoolFactory.class);

    protected static Connection connection;

    static{
        factory.setUsername("mqtt");
        factory.setPassword("mqtt");
        factory.setVirtualHost("mqtt");
        factory.setAutomaticRecoveryEnabled(true);
        factory.setNetworkRecoveryInterval(1000);

        String mqHost = System.getProperty("mqHost");
        if(mqHost != null)
            factory.setHost(mqHost);
        String mqPort = System.getProperty("mqPort");
        if(mqPort!= null){
            factory.setPort(Integer.valueOf(mqPort));
        }

        try {
            connection = factory.newConnection();
        } catch (IOException e) {
            logger.error("connect rabbit mq error",e);
        }

        Runtime.getRuntime().addShutdownHook(new ShutDown());
    }

    @Override
    public Channel create() throws Exception {
        return connection.createChannel();
    }

    @Override
    public PooledObject<Channel> wrap(Channel channel) {
        return new DefaultPooledObject<Channel>(channel);
    }

    @Override
    public void destroyObject(PooledObject<Channel> p) throws Exception {
        p.getObject().close();
    }

}
class ShutDown extends Thread{
    protected static final Logger logger = LoggerFactory.getLogger(MqPoolFactory.class);
    @Override
    public void run() {
        if(MqPoolFactory.connection != null) {

            logger.info("shut down rabbit mq connection");
            try {
                MqPoolFactory.connection.close();

            } catch (IOException e) {
                logger.error("close rabbit mq connection error !", e);
            }
            MqPoolFactory.connection = null;
        }
    }
}

