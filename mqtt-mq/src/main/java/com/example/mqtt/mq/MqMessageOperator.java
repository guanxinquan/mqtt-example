package com.example.mqtt.mq;

import com.rabbitmq.client.*;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Created by guanxinquan on 15-5-22.
 */
public class MqMessageOperator {

    private static final Logger logger = LoggerFactory.getLogger(MqMessageOperator.class);

    private GenericObjectPool<Channel> pool;

    public MqMessageOperator() {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxIdle(Integer.MAX_VALUE);
        config.setMaxTotal(10);
        config.setMinIdle(5);
        config.setBlockWhenExhausted(false);
        pool = new GenericObjectPool<Channel>(new MqPoolFactory(),config);


    }

    public MqMessageOperator(GenericObjectPoolConfig config) {
        pool = new GenericObjectPool<Channel>(new MqPoolFactory(),config);
    }

    public void addListeners(String queueName,final List<IMqListener> listeners) throws Exception {
        final Channel channel = pool.borrowObject();
        channel.basicConsume(queueName,false,new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                for(IMqListener listener: listeners){
                    try {
                        listener.listener(body);
                    }catch (Exception e){
                        logger.error("rabbit mq listener error ",e);
                    }
                }
                channel.basicAck(envelope.getDeliveryTag(),false);
            }
        });

    }

    public boolean publish(String exchangeName,String routingKey,BasicProperties props,byte[] messageBodyBytes) throws Exception {
        Channel channel = null;
        try{
            channel = pool.borrowObject();
            channel.basicQos(1);//每次只取一条
            channel.basicPublish(exchangeName,routingKey, (AMQP.BasicProperties) props,messageBodyBytes);
            return true;
        }catch (Exception e){
            logger.error("publish message to rabbit mq error ",e);
            return false;
        }finally{
            if(channel != null){
                pool.returnObject(channel);
            }
        }
    }


}
