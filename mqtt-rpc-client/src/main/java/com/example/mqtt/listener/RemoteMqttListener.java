package com.example.mqtt.listener;

import com.example.mqtt.api.IMqttListener;
import com.example.mqtt.api.IMqttRemoteListener;
import com.example.mqtt.event.listener.ListenerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by guanxinquan on 15-5-11.
 */
public class RemoteMqttListener extends UnicastRemoteObject implements IMqttRemoteListener{

    private static final Logger logger = LoggerFactory.getLogger(RemoteMqttListener.class);

    private IMqttListener listener = null;

    public RemoteMqttListener(IMqttListener listener) throws RemoteException {
        this.listener = listener;
    }

    @Override
    public Object eventArrival(ListenerEvent event) throws RemoteException {

        logger.debug("listener event {} {}", event.getClientID(), event.getUserID());

        Object object = null;
        if(listener != null){
            try {
                object = listener.eventArrival(event);
            }catch (Exception e){
                logger.error("error event ",e);
            }
        }
        return object;
    }

}
