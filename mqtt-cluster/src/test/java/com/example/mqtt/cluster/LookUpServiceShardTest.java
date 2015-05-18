package com.example.mqtt.cluster;

import com.example.mqtt.api.IMqttService;
import com.example.mqtt.event.mqtt.PublishEvent;

import java.rmi.Naming;

/**
 * Created by guanxinquan on 15-5-13.
 */
public class LookUpServiceShardTest {

    public static void main(String[] args) throws Exception {
        //System.setProperty("zk","localhost:2181");
        //LookupService service =  LookupServiceFactory.getInstance();

        //IMqttService mqttService = service.lookup(IMqttService.class);


        //IMqttRemoteListener listener = service.lookup(IMqttRemoteListener.class);

        while(true){
           // String url = service.lookUpServer("121231231");
            //IMqttService s = service.lookup("121231312", IMqttService.class);
            IMqttService s = (IMqttService) Naming.lookup("rmi://123.126.105.45:2088/com.example.mqtt.api.IMqttService");
            s.sendEvent(new PublishEvent(9998l,"hello world".getBytes(),"just for topic"));
            System.out.println("finish once");
            //System.out.println(url);
            Thread.sleep(10000l);
        }
    }
}
