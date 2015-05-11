package com.example.mqtt.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by guanxinquan on 15-5-7.
 *
 * 基于zk的注册系统
 *
 */
public class ZkServer implements IZkServer{

    public static final int DEFAULT_RETRY_TIMES = 500000;
    public static final int DEFAULT_RETRY_DURATION = 10000;
    public static final int DEFAULT_CONNECTION_TIME_OUT = 30000;



    private static final String NAME_SPACE = "mqtt/cluster";

    private static final String SERVER_PATH = "/server";

    private static final Logger logger = LoggerFactory.getLogger(ZkServer.class);

    private CuratorFramework framework;

    private String hosts;

    private PathChildrenCache serverPath;

    private Map<String,PathChildrenCache> apiPaths = new ConcurrentHashMap<String, PathChildrenCache>();

    private static final Integer lock = new Integer(1);


    public ZkServer(String hosts) {
        this.hosts = hosts;
        init();
    }

    public void init(){
        logger.info("init zk host connections hosts{}",hosts);
        framework = CuratorFrameworkFactory.builder().
                connectString(hosts).
                namespace(NAME_SPACE).
                retryPolicy(new RetryNTimes(this.DEFAULT_RETRY_TIMES, this.DEFAULT_RETRY_DURATION)).
                connectionTimeoutMs(DEFAULT_CONNECTION_TIME_OUT).
                build();
        framework.start();

        serverPath = new PathChildrenCache(framework,SERVER_PATH,true);
        try {
            serverPath.getListenable().addListener(new ServerChangeListener());
            serverPath.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<ChildData> fetchApiProvider(String apiName) throws Exception {
        PathChildrenCache cache = apiPaths.get(apiName);
        if(cache == null)
        {
            String path = SERVER_PATH+ "/" + apiName;
            Stat stat = framework.checkExists().forPath(path);
            if(stat == null){
                return Collections.EMPTY_LIST;
            }else{
                synchronized (lock){
                    if (!apiPaths.containsKey(apiName)) {
                        PathChildrenCache apiCache = new PathChildrenCache(framework, path, true);
                        apiCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
                        apiPaths.put(apiName, apiCache);
                    }
                }
            }
        }
        if(cache == null)
            cache = apiPaths.get(apiName);
        return cache.getCurrentData();
    }

    public void registerApiProvider(String api,String host,Integer port,byte[] data) throws Exception {
        String className = api;
        String url = String.format("rmi://%s:%d/%s",host,port,className).replace('/','_');
        String path = SERVER_PATH+"/"+className;
        Stat stat = framework.checkExists().forPath(path);
        if(stat == null){
            createNode(path,CreateMode.PERSISTENT);
        }
        String subPath = path + "/"+url;
        stat = framework.checkExists().forPath(subPath);
        if(stat == null){
            createNode(subPath,CreateMode.EPHEMERAL);
            if(data != null){
                framework.setData().forPath(subPath,data);
            }
        }

    }

    private void createNode(String path,CreateMode mode) throws Exception {
        framework.
                create().
                creatingParentsIfNeeded().
                withMode(mode).
                withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE).
                forPath(path);
    }

    @Override
    public void close() throws IOException {
        try {
            serverPath.close();
            for (Map.Entry<String,PathChildrenCache> e : apiPaths.entrySet()){
                if(e.getValue() != null)
                    e.getValue().close();
            }
        }catch (Exception e){

        }finally {
            framework.close();
        }

    }

    class ServerChangeListener implements PathChildrenCacheListener{

        @Override
        public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
            String path = pathChildrenCacheEvent.getData().getPath();
            String api = path.split("/")[1];
            synchronized (lock) {
                if (!apiPaths.containsKey(api)) {
                    PathChildrenCache apiCache = new PathChildrenCache(curatorFramework, SERVER_PATH + "/" + api, true);
                    apiPaths.put(api, apiCache);
                }

            }
        }
    }


}
