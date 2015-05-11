package com.example.mqtt.api;

import com.example.mqtt.event.listener.DisconnectEvent;
import com.example.mqtt.event.mqtt.KickEvent;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Created by guanxinquan on 15-5-11.
 */
public class RMIClientTest {

    public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException {
        int port = 1099;
        String listenerUrl = String.format("rmi://localhost:%d/%s",port,IMqttRemoteListener.class.getTypeName());
        String serviceUrl = String.format("rmi://localhost:%d/%s",port,IMqttService.class.getTypeName());


        IMqttRemoteListener listener = (IMqttRemoteListener) Naming.lookup(listenerUrl);
        IMqttService service = (IMqttService) Naming.lookup(serviceUrl);

        listener.eventArrival(new DisconnectEvent("9999",999l));

        service.sendEvent(new KickEvent("9999",999l));


    }
}
