package com.example.mqtt.rpc;


import com.example.mqtt.api.IMqttListener;
import com.example.mqtt.api.IMqttRemoteListener;
import com.example.mqtt.cluster.LookupService;
import com.example.mqtt.cluster.LookupServiceFactory;
import com.example.mqtt.event.listener.ListenerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;

/**
 * Created by guanxinquan on 15-5-12.
 *
 * mqtt内部使用的listener，用于通知远程服务，有新的消息到来
 *
 */
public class MqttListener implements IMqttListener {

    private LookupService lookupService = LookupServiceFactory.getInstance();

    private static final Logger logger = LoggerFactory.getLogger(MqttListener.class);



    @Override
    public Object eventArrival(ListenerEvent event) throws RemoteException {
        IMqttRemoteListener listener = getService(IMqttRemoteListener.class);
        if(listener == null){//如果第一次取的服务为null，尝试重新取一次
            listener = getService(IMqttRemoteListener.class);
        }
        if(listener != null){
            return listener.eventArrival(event);
        }
        return null;
    }

    private <T> T getService(Class<T> clazz){
        T service = null ;
        try {
            service = (T) lookupService.lookup(clazz);
        } catch (Exception e) {
            logger.error("get remote service error ",e);
        }
        return service;
    }
}
