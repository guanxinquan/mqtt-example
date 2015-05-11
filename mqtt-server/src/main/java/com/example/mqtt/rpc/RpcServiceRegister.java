package com.example.mqtt.rpc;

import com.example.mqtt.api.IMqttRemoteListener;
import com.example.mqtt.api.IMqttService;
import com.example.mqtt.zk.IZkServer;
import com.example.mqtt.zk.ZkServerFactory;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 * Created by guanxinquan on 15-5-11.
 */
public class RpcServiceRegister {

    private static final IZkServer zkServer = ZkServerFactory.getInstance();

    static {
        String url = String.format("rmi://localhost:%d/%s",1088,IMqttService.class.getTypeName());
        try {
            IMqttService mqttService = new MqttServerImpl();
            LocateRegistry.createRegistry(1088);
            Naming.rebind(url, mqttService);
            zkServer.registerApiProvider(IMqttService.class.getTypeName(),"localhost",1088,null);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void register(){

    }


}
