package com.example.mqtt.listener;

import com.example.mqtt.api.IMqttRemoteListener;

import java.rmi.RemoteException;

/**
 * Created by guanxinquan on 15-5-11.
 */
public class IRemoteMqttListenerTest {

    public static void main(String[] args) throws InterruptedException, RemoteException {
        System.setProperty("zk","localhost:2181");
        IMqttRemoteListener listener = RemoteMqttListenerFactory.getRemoteMqttListener();
        while(true){
            listener.getListeners();
            Thread.sleep(10000l);
        }
    }
}
