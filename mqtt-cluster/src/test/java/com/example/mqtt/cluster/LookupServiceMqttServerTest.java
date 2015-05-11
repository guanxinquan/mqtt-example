package com.example.mqtt.cluster;

import com.example.mqtt.api.IMqttRemoteListener;
import com.example.mqtt.api.IMqttService;
import com.example.mqtt.event.listener.DisconnectEvent;
import com.example.mqtt.event.mqtt.PublishEvent;

/**
 * Created by guanxinquan on 15-5-11.
 */
public class LookupServiceMqttServerTest {


    public static void main(String[] args) throws Exception {
        System.setProperty("zk","localhost:2181");
        LookupService service =  LookupServiceFactory.getInstance();

        IMqttService mqttService = service.lookup(IMqttService.class);

        //IMqttRemoteListener listener = service.lookup(IMqttRemoteListener.class);

        while(true){
            PublishEvent event = new PublishEvent();
            event.setUserID(9999l);
            event.setClientID("88888");

            mqttService.sendEvent(event);
            Thread.sleep(10000l);
        }
    }
}
