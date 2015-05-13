package com.example.mqtt.store.impl;

import com.example.mqtt.spi.IMqttService;
import com.example.mqtt.store.QosFlightStore;
import com.example.mqtt.store.QosPubStoreEvent;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by guanxinquan on 15-5-7.
 */
public class QosFlightStoreImpl implements QosFlightStore {

    private static final Logger logger = LoggerFactory.getLogger(QosFlightStoreImpl.class);

    private static final long RETRY_TIME = 2000;

    private ConcurrentLinkedQueue<QosPubStoreEvent> eventQueue = new ConcurrentLinkedQueue<QosPubStoreEvent>();

    private Cache<String,QosPubStoreEvent> eventCache;

    private boolean running = true;

    private IMqttService service;

    private Worker worker;

    public QosFlightStoreImpl() {

        eventCache = CacheBuilder.newBuilder().expireAfterWrite((long) (RETRY_TIME * 2.5), TimeUnit.MILLISECONDS).build();
        worker = new Worker();
        worker.start();

    }

    @Override
    public void storeFlight(String clientID, String messageID, QosPubStoreEvent event) {
        logger.debug("store flight clientId {} messageId {} data {}",clientID,messageID,new String(event.getMessage().getPayload().array()));
        String key = getCacheKey(clientID, messageID);
        QosPubStoreEvent oldEvt = eventCache.getIfPresent(key);
        if(oldEvt != null){
            logger.info("store flight ,clientID {} messageID {} data {} conflict old evt will ignore ",clientID,messageID,new String(event.getMessage().getPayload().array()));
            oldEvt.setValidate(false);
        }
        event.setExpireTime(System.currentTimeMillis() + RETRY_TIME);
        eventCache.put(key, event);
        eventQueue.add(event);
    }

    @Override
    public void removeFlight(String clientID, String messageID) {
        logger.debug("remove flight clientId {} messageId {}",clientID,messageID);
        String key = getCacheKey(clientID,messageID);
        QosPubStoreEvent event = eventCache.getIfPresent(key);
        if(event != null){
            logger.debug("remove flight invalidate clientId {} messageId {}",clientID,messageID);
            eventCache.invalidate(key);
            event.setValidate(false);
        }else{
            logger.debug("remove flight but flight message already remove clientId {} messageId {}",clientID,messageID);
        }
    }

    @Override
    public long getSize() {
        return eventCache.size();
    }

    private String getCacheKey(String clientID,String messageID){
        return clientID + ":"+messageID;
    }

    @Override
    public void close() throws IOException {
        running = false;
        try {
            worker.join();
            worker.interrupt();
        } catch (InterruptedException e) {
        }

        eventCache.invalidateAll();
        eventCache.cleanUp();

    }

    class Worker extends Thread {
        public Worker(){
            super("qos cache");
            setDaemon(true);
        }

        @Override
        public void run() {
            while(running){
                try {
                    QosPubStoreEvent evt = eventQueue.peek();
                    if (evt != null) {
                        long distance = evt.getExpireTime() - System.currentTimeMillis();
                        if (distance > 0) {
                            logger.debug("queue top elements not expire,will sleep {}", distance);
                            sleep(distance + 1);
                        }else{
                            logger.debug("queue top elements republish clientId {}", evt.getClientID());
                            eventQueue.poll();
                            if(evt.isValidate()){
                                logger.debug("queue top retry {}",evt.getClientID());

                                boolean retry = service.republish(evt);
                                if(retry){
                                    if(evt.getCnt() < 2){
                                        evt.setCnt(evt.getCnt()+1);
                                        evt.setExpireTime(System.currentTimeMillis() + RETRY_TIME);
                                        logger.debug("queue to rescheduler clientId {}",evt.getClientID());
                                        eventQueue.add(evt);
                                    }else{

                                        logger.info("retry three times for push message but not recieve ack ,kick out the client {}",evt.getClientID());
                                        service.kickOut(evt.getClientID());
                                    }
                                }else{
                                    logger.debug("message retry but client already closed clientId {}",evt.getClientID());
                                }
                            }else{
                                logger.debug("message retry but message already invalidate clientId {}",evt.getClientID());
                            }
                        }
                    } else {
                        logger.debug("no flight message should be process , sleep {} ",RETRY_TIME);
                        sleep(RETRY_TIME);
                    }
                }catch (Exception e){
                    logger.error("error ",e);
                }
            }

        }
    }
}
