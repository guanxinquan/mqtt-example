package com.example.mqtt.api;

import com.example.mqtt.event.listener.ListenerEvent;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Created by guanxinquan on 15-5-11.
 *
 * 远程监听，用于客户端实现监听rpc服务
 *
 */
public interface IMqttRemoteListener extends Remote{

    public Object eventArrival(ListenerEvent event) throws RemoteException;

}
