package com.example.mqtt.zk;

import org.apache.curator.framework.recipes.cache.ChildData;

import java.io.Closeable;
import java.util.Date;
import java.util.List;

/**
 * Created by guanxinquan on 15-5-8.
 *
 * zk服务器
 */
public interface IZkServer extends Closeable{

    /**
     * 将服务注册到zk上，主要用于rpc服务端的注册
     * @param api 注册服务的类型
     * @param host 注册服务的主机
     * @param port 注册服务的端口
     * @param data 注册服务时需要的附加数据
     * @throws Exception
     */
    public void registerApiProvider(String api,String host,Integer port,byte[] data) throws Exception;


    /**
     * 获取rpc服务的配置
     * @param apiName 服务的类名字
     * @return 配置列表
     * @throws Exception
     */
    public List<ChildData> fetchApiProvider(String apiName) throws Exception;

    /**
     * 注册服务
     * @param server
     * @param data
     */
    public void registerServer(String server,byte[] data) throws Exception;

    /**
     * 按照某个key值获取server
     * @param key
     * @return
     */
    public String fetchServer(String key);
}
