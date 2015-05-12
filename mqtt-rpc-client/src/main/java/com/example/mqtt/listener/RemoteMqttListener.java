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

    private List<IMqttListener> listeners = new ArrayList<IMqttListener>();

    public RemoteMqttListener() throws RemoteException {

    }

    @Override
    public Object eventArrival(ListenerEvent event) throws RemoteException {

        logger.debug("listener event {} {}", event.getClientID(), event.getUserID());

        Object object = null;
        for (IMqttListener listener : listeners){//一直遍历，知道有某一个返回值为飞空的listener。
            object = listener.eventArrival(event);
            if(object != null){
                break;
            }
        }


        return object;
    }



    @Override
    public List<IMqttListener> getListeners() {
        return listeners;
    }
}
