package com.example.mqtt.cluster;

import com.example.mqtt.zk.IZkServer;
import com.example.mqtt.zk.ZkServerFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    @Override
    public <T> T lookup(Class<T> clazz) throws Exception {
        T service = null;
        List<ChildData> childData =  zkServer.fetchApiProvider(clazz.getTypeName());
        int size = childData.size();
        String url = null;
        if(size == 1){
            url = generatorUrl(childData.get(0).getPath());
        }else{
            url = generatorUrl(childData.get(ThreadLocalRandom.current().nextInt(size)).getPath());
        }
        logger.debug("use remote url {}",url);
        if(url != null){
            service = (T) Naming.lookup(url);
        }

        return service;
    }

    private String generatorUrl(String fullPath){
        String[] split = fullPath.split("/");
        String url = split[split.length -1];
        return url.replace('_','/');
    }
}
