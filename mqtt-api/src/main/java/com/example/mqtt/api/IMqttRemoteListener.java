package com.example.mqtt.api;

import com.example.mqtt.event.listener.ListenerEvent;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Created by guanxinquan on 15-5-11.
 *
 * mqtt事件监听器
 *
 */
public interface IMqttRemoteListener extends Remote{

    public Object eventArrival(ListenerEvent event) throws RemoteException;

    public List<IMqttListener> getListeners()throws RemoteException;
}
