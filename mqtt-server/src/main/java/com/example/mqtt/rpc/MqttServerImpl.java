package com.example.mqtt.rpc;

import com.example.mqtt.api.IMqttService;
import com.example.mqtt.event.mqtt.MqttEvent;
import com.example.mqtt.event.mqtt.PublishEvent;
import com.example.mqtt.spi.IMessageFactory;
import com.example.mqtt.spi.IMessaging;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by guanxinquan on 15-5-8.
 *
 * 远程调用服务端的接口
 *
 */
public class MqttServerImpl extends UnicastRemoteObject implements IMqttService {

    private static final Logger logger = LoggerFactory.getLogger(MqttServerImpl.class);

    public MqttServerImpl() throws RemoteException {
    }

    IMessaging messaging = IMessageFactory.getInstance();

    @Override
    public Object sendEvent(MqttEvent event) throws RemoteException {
        if(event instanceof PublishEvent){
            PublishEvent e  = (PublishEvent) event;
            logger.info("mqtt service receive rpc publish event {} {}",event.getUserID(),event.getClientID());
            if(e.getClientID() == null ||"".equals(e.getClientID())){//send message by userId
                messaging.sendMessageByUser(String.valueOf(e.getUserID()),e.getPayLoad(),e.getTopic());
            }else{//send message by clientId
                messaging.sendMessage(e.getClientID(),e.getPayLoad(),e.getTopic());
            }
        }
        return null;
    }
}
