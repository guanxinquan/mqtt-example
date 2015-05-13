package com.example.mqtt.store;

import java.io.Closeable;

/**
 * Created by guanxinquan on 15-5-7.
 */
public interface PubStubStore extends Closeable{

    public void storeStub(String clientID,String messageID);

    public String fetchStub(String clientID,String messageID);

    public long getSize();

}
