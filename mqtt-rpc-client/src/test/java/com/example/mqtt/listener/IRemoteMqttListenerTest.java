package com.example.mqtt.listener;

import com.example.mqtt.api.IMqttListener;
import com.example.mqtt.api.IMqttRemoteListener;
import com.example.mqtt.api.IMqttService;
import com.example.mqtt.event.listener.DisconnectEvent;
import com.example.mqtt.event.listener.ListenerEvent;
import com.example.mqtt.event.listener.LoginEvent;
import com.example.mqtt.event.listener.PublishEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.rmi.RemoteException;

/**
 * Created by guanxinquan on 15-5-11.
 */
public class IRemoteMqttListenerTest {

    private static final Logger logger = LoggerFactory.getLogger(IRemoteMqttListenerTest.class);

    public static void main(String[] args) throws InterruptedException, RemoteException {
        System.setProperty("zk","localhost:2181");
        System.setProperty("rmiHost", "localhost");
        System.setProperty("rmiPort","1099");
        IMqttRemoteListener listener = RemoteMqttListenerFactory.getRemoteMqttListener();
        listener.getListeners().add(new IMqttListener() {
            IMqttService service = new MqttServer();

            @Override
            public Object eventArrival(ListenerEvent event) {
                if(event instanceof DisconnectEvent){
                    logger.info("disconnect event arrival clientId {} userId {}",event.getClientID(),event.getUserID());
                }else if(event instanceof LoginEvent){
                    LoginEvent e = (LoginEvent) event;
                    logger.info("login event arrival clientId {} userId {} password {}",e.getClientID(),e.getUserID(),e.getPassword());
                    if(e.getClientID().equals(String.valueOf(e.getPassword()))){
                        return new Boolean(true);
                    }else{
                        return new Boolean(false);
                    }
                }else if(event instanceof PublishEvent){
                    PublishEvent e = (PublishEvent) event;
                    logger.info("publish event arrival clientId {} userId{} topic {} message {}",e.getClientID(),e.getUserID(),e.getTopic(),new String(e.getPayLoad(), Charset.forName("utf-8")));
                    try {
                        service.sendEvent(new com.example.mqtt.event.mqtt.PublishEvent(e.getClientID(),e.getUserID(),e.getPayLoad(),e.getTopic()));
                    } catch (RemoteException e1) {
                        e1.printStackTrace();
                    }

                }
                return null;
            }
        });
        while(true){
            listener.getListeners();
            Thread.sleep(10000l);
        }
    }
}
