package com.example.mqtt.cluster;

/**
 * Created by guanxinquan on 15-5-11.
 */
public class LookupServiceFactory {

    private static LookupService instance = new RmiLookupServiceImpl();

    public static final LookupService getInstance(){
        return instance;
    }

}
