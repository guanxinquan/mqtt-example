package com.example.mqtt.cluster;

import com.example.mqtt.api.IMqttRemoteListener;
import com.example.mqtt.event.listener.DisconnectEvent;

/**
 * Created by guanxinquan on 15-5-11.
 */
public class LookupServiceClientTest {

    public static void main(String[] args) throws Exception {
        System.setProperty("zk","localhost:2181");
        LookupService service =  LookupServiceFactory.getInstance();

        IMqttRemoteListener listener = service.lookup(IMqttRemoteListener.class);

        while(true){
            listener.eventArrival(new DisconnectEvent("9999",9998l));
            Thread.sleep(10000l);
        }
    }


}
