package com.example.mqtt.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


/**
 * Created by guanxinquan on 15-5-21.
 */
public class RabbitmqTest {

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("mqtt");
        factory.setPassword("mqtt");
        factory.setVirtualHost("mqtt");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
       // channel.queueDeclare("mqtt-queue",true,false,false,null);
        String message = "hello world";
        channel.basicPublish("mqtt-pub","mqtt-queue",MessageProperties.PERSISTENT_BASIC,message.getBytes());
        channel.close();
        connection.close();
    }
}
