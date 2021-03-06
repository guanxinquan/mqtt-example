package com.example.mqtt.cluster;

import com.example.mqtt.zk.IZkServer;
import com.example.mqtt.zk.ZkServerFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.rmi.Naming;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by guanxinquan on 15-5-8.
 * 用于从zk上获取注册的服务
 */
public class RmiLookupServiceImpl implements LookupService {

    private static final Logger logger = LoggerFactory.getLogger(RmiLookupServiceImpl.class);

    private IZkServer zkServer = ZkServerFactory.getInstance();

    /**
     * 随机获取一个远程调用服务
     * @param clazz
     * @param <T>
     * @return
     * @throws Exception
     */
    @Override
    public <T> T lookup(Class<T> clazz) throws Exception {
        T service = null;
        List<ChildData> childData =  zkServer.fetchApiProvider(clazz.getTypeName());
        int size = childData.size();
        String url = null;
        if(size == 1){
            url = generatorUrl(childData.get(0).getPath());
        }else if(size > 1){
            url = generatorUrl(childData.get(ThreadLocalRandom.current().nextInt(size)).getPath());
        }else{
            logger.error("no provider of service {} exist ,please check out service ",clazz.getTypeName(),new Exception());
            return service;
        }
        logger.debug("use remote url {}",url);
        if(url != null){
            logger.info("connect rpc url is {}",url);
            service = (T) Naming.lookup(url);
        }

        return service;
    }

    @Override
    public <T> T lookup(String host, String port, Class<T> clazz) throws Exception {

        T server = null;
        String url = String.format("rmi://%s:%s/%s",host,port,clazz.getTypeName());

        server = (T) Naming.lookup(url);

        return server;
    }

    @Override
    public <T> T lookup(String key, Class<T> clazz) throws Exception {
        String service = zkServer.fetchServer(key);
        if(service != null){
            String[] split = service.split(":");
            String host = split[0];
            String port = split[1];
            String rmiPort = split[2];

            return lookup(host,rmiPort,clazz);
        }
        return null;
    }

    @Override
    public String lookUpServer(String key) throws Exception {
        String srv = zkServer.fetchServer(key);
        if(srv != null){
            String[] split = srv.split(":");
            return split[0]+":"+split[1];
        }
        return null;
    }


    private String generatorUrl(String fullPath){
        String[] split = fullPath.split("/");
        String url = split[split.length -1];
        return url.replace('_','/');
    }
}
