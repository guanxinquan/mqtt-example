package com.example.mqtt.store.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.example.mqtt.store.PubStubStore;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by guanxinquan on 15-5-7.
 */
public class GuavaPubStubStore implements PubStubStore {

    private static final long DEFAULT_EXPIRE_TIME = 5000;//过期时间为5s

    private Cache<String,String> cache;

    public GuavaPubStubStore() {
        this(DEFAULT_EXPIRE_TIME);
    }

    public GuavaPubStubStore(long expire){
        cache = CacheBuilder.newBuilder().expireAfterWrite(expire, TimeUnit.MILLISECONDS).build();
    }

    @Override
    public void storeStub(String clientID, String messageID) {
        cache.put(getCacheKey(clientID,messageID),"");
    }

    @Override
    public String fetchStub(String clientID, String messageID) {
        return cache.getIfPresent(getCacheKey(clientID,messageID));
    }

    @Override
    public void close() throws IOException {
        cache.invalidateAll();
        cache.cleanUp();
    }

    private String getCacheKey(String clientID,String messageID){
        return clientID+":"+messageID;
    }
}
