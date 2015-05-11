package com.example.mqtt.api;

import com.example.mqtt.event.listener.ListenerEvent;
import com.example.mqtt.event.mqtt.MqttEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/**
 * Created by guanxinquan on 15-5-11.
 */
public class RMIServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(RMIServiceTest.class);

    public static void main(String[] args) throws RemoteException, MalformedURLException {
        int port = 1099;
        String listenerUrl = String.format("rmi://localhost:%d/%s",port,IMqttRemoteListener.class.getTypeName());
        String serviceUrl = String.format("rmi://localhost:%d/%s",port,IMqttService.class.getTypeName());
        LocateRegistry.createRegistry(1099);
        Naming.rebind(listenerUrl,new MqttListener());
        Naming.rebind(serviceUrl,new MqttService());
    }



}
class MqttListener extends UnicastRemoteObject implements IMqttRemoteListener {

    public MqttListener() throws RemoteException {

    }

    private static final Logger logger = LoggerFactory.getLogger(MqttListener.class);
    @Override
    public Object eventArrival(ListenerEvent event) throws RemoteException {
        logger.info("event arrival {} {}",event.getClientID(),event.getUserID());
        return new Boolean(true);
    }

    @Override
    public List<IMqttListener> getListeners() throws RemoteException {
        return null;
    }
}

class MqttService extends UnicastRemoteObject implements IMqttService{
    private static final Logger logger = LoggerFactory.getLogger(MqttService.class);

    public MqttService() throws RemoteException {
    }

    @Override
    public Object sendEvent(MqttEvent event) throws RemoteException {
        logger.info("send event {} {} ",event.getClientID(),event.getUserID());
        return null;
    }
}
