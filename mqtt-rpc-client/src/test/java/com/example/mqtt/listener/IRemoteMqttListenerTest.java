package com.example.mqtt.listener;

import com.example.mqtt.api.IMqttListener;
import com.example.mqtt.api.IMqttRemoteListener;
import com.example.mqtt.api.IMqttService;
import com.example.mqtt.event.listener.*;
import com.example.mqtt.event.listener.PublishEvent;
import com.example.mqtt.event.mqtt.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by guanxinquan on 15-5-11.
 */
public class IRemoteMqttListenerTest {

    private static final Logger logger = LoggerFactory.getLogger(IRemoteMqttListenerTest.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws InterruptedException, RemoteException {
        System.setProperty("zk","localhost:2181");
        System.setProperty("rmiHost", "192.168.2.99");
        System.setProperty("rmiPort","1099");

        RemoteMqttListenerFactory.start(new IMqttListener() {
            IMqttService service = new MqttServer();
            Map<Long,Vector<byte[]>> messages = new HashMap<Long, Vector<byte[]>>();

            Integer lock = new Integer(0);


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
                        return new Boolean(true);
                    }
                }else if(event instanceof PublishEvent){
                    PublishEvent e = (PublishEvent) event;
                    logger.info("publish event arrival clientId {} userId{} topic {} message {}",e.getClientID(),e.getUserID(),e.getTopic(),new String(e.getPayLoad(), Charset.forName("utf-8")));
                    try {
                        synchronized (lock) {
                            Vector<byte[]> ms = messages.get(e.getUserID());
                            if (ms == null) {
                                ms = new Vector<byte[]>();
                                messages.put(e.getUserID(), ms);
                            }
                            ms.add(e.getPayLoad());//将消息存储
                            service.sendEvent(new SyncDownEvent(e.getUserID()));//告诉mqtt服务器，过来同步消息
                        }
                        //service.sendEvent(new com.example.mqtt.event.mqtt.PublishEvent(null,e.getUserID(),e.getPayLoad(),e.getTopic()));
                        //service.sendEvent(new com.example.mqtt.event.mqtt.PublishEvent(e.getClientID(),e.getUserID(),e.getPayLoad(),e.getTopic()));
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }

                }else if(event instanceof SyncUpEvent){
                    SyncUpEvent e = (SyncUpEvent) event;
                    String syncTag = e.getSyncTag();
                    Integer syncId = Integer.valueOf(syncTag);
                    synchronized (lock) {
                        Vector<byte[]> message = messages.get(e.getUserID());

                        if (message == null || message.size() == 0 || message.size() < syncId) {
                            logger.info("sync up event syncTag {} clientId {} userId {},but no message should be sync ", e.getSyncTag(), e.getClientID(), e.getUserID());
                            return null;
                        }

                        List<byte[]> m = message.subList(syncId, message.size());

                        if (m.size() < 1) {
                            return null;
                        }

                        try {
                            //byte[] bs = mapper.writeValueAsBytes(m);
                            StringBuilder content = new StringBuilder();
                            for (byte[] o : m) {
                                content.append(new String(o, Charset.forName("utf-8")));
                                content.append(",");
                            }
                            service.sendEvent(new com.example.mqtt.event.mqtt.PublishEvent(e.getClientID(), e.getUserID(), content.toString().getBytes(), String.valueOf(message.size()),e.getSyncTag()));
                            logger.info("sync up event syncTag {} clientId {} userId {},message count is {}", e.getSyncTag(), e.getClientID(), e.getUserID(), m.size());
                        } catch (Exception e1) {
                            logger.error("publish date error !", e1);
                        }
                    }

                }
                return null;
            }
        });

    }
}
