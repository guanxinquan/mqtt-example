package com.example.mqtt.listener;

import com.example.mqtt.api.IMqttRemoteListener;
import com.example.mqtt.zk.IZkServer;
import com.example.mqtt.zk.ZkServerFactory;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 * Created by guanxinquan on 15-5-11.
 */
public class RemoteMqttListenerFactory {

    private static IMqttRemoteListener listener ;
    private static IZkServer zkServer = ZkServerFactory.getInstance();

    static {
        String url = String.format("rmi://localhost:%d/%s",1099,IMqttRemoteListener.class.getTypeName());
        try {
            listener = new RemoteMqttListener();
            LocateRegistry.createRegistry(1099);
            Naming.rebind(url,listener);
            zkServer.registerApiProvider(IMqttRemoteListener.class.getTypeName(),"localhost",1099,null);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final IMqttRemoteListener getRemoteMqttListener(){
        return listener;
    }
}
