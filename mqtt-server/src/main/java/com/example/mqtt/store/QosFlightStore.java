package com.example.mqtt.store;

import java.io.Closeable;

/**
 * Created by guanxinquan on 15-5-7.
 */
public interface QosFlightStore extends Closeable{

    public void storeFlight(String clientID,String messageID,QosPubStoreEvent store);

    public void removeFlight(String clientID,String messageID);

    public long getSize();

}
