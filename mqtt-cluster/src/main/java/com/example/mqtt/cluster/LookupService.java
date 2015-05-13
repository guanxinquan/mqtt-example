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

    /**
     * 直连的方式链接远程服务
     * @param host 需要链接的host
     * @param port 需要链接的port
     * @param clazz 提供服务的类
     * @param <T>
     * @return
     * @throws Exception
     */
    public <T> T lookup(String host,String port,Class<T> clazz) throws Exception;

    /**
     * 通过一致性hash算法获取到远程服务
     * @param key hash的key
     * @param clazz 提供服务的类
     * @param <T>
     * @return
     * @throws Exception
     */
    public <T> T lookup(String key,Class<T> clazz) throws Exception;

    /**
     * 通过制定的key，查找服务
     * @param key
     * @return
     * @throws Exception
     */
    public String lookUpServer(String key) throws Exception;

}
