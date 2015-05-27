package com.example.mqtt.api;

import com.example.mqtt.event.listener.ListenerEvent;

import java.rmi.RemoteException;

/**
 * Created by guanxinquan on 15-5-11.
 * 监听远程mqtt服务的消息，用于客户端添加监听事件
 *
 */
public interface IMqttListener {
    /**
     * 有mqtt消息到来时，触发请求,客户端必须处理异常
     * @param event
     * @return
     */
    public Object eventArrival(ListenerEvent event) throws RemoteException;
}
