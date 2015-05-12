package com.example.mqtt.cluster;


/**
 * Created by guanxinquan on 15-5-8.
 * 用于获取远程调用的服务
 */
public interface LookupService {

    /**
     * 用于获取远程服务
     * @param clazz
     * @param <T>
     * @return
     * @throws Exception
     */
    public <T> T lookup(Class<T> clazz) throws Exception;

}
