package com.example.mqtt.api;



import com.example.mqtt.event.mqtt.MqttEvent;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by guanxinquan on 15-5-11.
 * 调用mqtt服务执行的事件
 */
public interface IMqttService extends Remote{

    public Object sendEvent(MqttEvent event) throws RemoteException;
}
