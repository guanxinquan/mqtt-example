package com.example.mqtt.listener;

import com.example.mqtt.api.IMqttService;
import com.example.mqtt.cluster.LookupService;
import com.example.mqtt.cluster.LookupServiceFactory;
import com.example.mqtt.event.mqtt.MqttEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;

/**
 * Created by guanxinquan on 15-5-12.
 * 用于调用远程mqtt服务，发送事件
 */
public class MqttServer implements IMqttService {

    private static final LookupService lookupService = LookupServiceFactory.getInstance();

    private static final Logger logger = LoggerFactory.getLogger(MqttServer.class);

    @Override
    public Object sendEvent(MqttEvent event) throws RemoteException {

        IMqttService service = lookupService(IMqttService.class);
        if (service != null){
            service.sendEvent(event);
        }else{
            logger.error("service is null {}",IMqttService.class.getTypeName(),new Exception());
        }
        return null;
    }

    private <T> T lookupService(Class<T> clazz){
        T service = null;
        try {
            service = lookupService.lookup(clazz);
        } catch (Exception e) {
            logger.error("look up service error ",e);
        }
        return  service;
    }
}
