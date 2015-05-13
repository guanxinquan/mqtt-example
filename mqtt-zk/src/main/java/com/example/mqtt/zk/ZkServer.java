package com.example.mqtt.zk;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
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
import java.nio.charset.Charset;
import java.util.*;
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

    private static final String MQTT_PATH = "/mqtt_server";

    private static final Logger logger = LoggerFactory.getLogger(ZkServer.class);

    private CuratorFramework framework;

    private String hosts;

    private PathChildrenCache serverPath;

    private PathChildrenCache mqttServerPath;

    private Map<String,PathChildrenCache> apiPaths = new ConcurrentHashMap<String, PathChildrenCache>();

    private static final Integer lock = new Integer(1);

    private static final Integer mqttLock = new Integer(1);

    private TreeMap<Long,String> nodes = new TreeMap<Long, String>();

    private HashFunction hash = Hashing.murmur3_128();



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

        //监控服务上层目录变化
        serverPath = new PathChildrenCache(framework,SERVER_PATH,true);
        try {
            serverPath.getListenable().addListener(new ServerChangeListener());
            serverPath.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        } catch (Exception e) {
            logger.error("server path listener error ",e);
            System.exit(1);
        }

        mqttServerPath = new PathChildrenCache(framework,MQTT_PATH,true);
        try{
            mqttServerPath.getListenable().addListener(new MqttServiceChangeListener());
            mqttServerPath.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
            syncHashData();
        }catch (Exception e){
            logger.error("mqtt server path listener error ",e);
            System.exit(1);
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
                        apiCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);//在启动时，加载路径
                        apiPaths.put(apiName, apiCache);
                    }
                }
            }
        }
        if(cache == null)
            cache = apiPaths.get(apiName);
        return cache.getCurrentData();
    }

    @Override
    public void registerServer(String server, byte[] data) throws Exception {
        String path = MQTT_PATH+"/"+server;
        Stat stat = framework.checkExists().forPath(path);
        if (stat == null){
            createNode(path,CreateMode.EPHEMERAL);
            if(data != null){
                framework.setData().forPath(path,data);
            }
        }else{
            throw new Exception("register server already register "+server);
        }
    }

    @Override
    public String fetchServer(String key) {

        synchronized (mqttLock) {
            SortedMap<Long, String> tail = nodes.tailMap(hash.hashString(key, Charset.forName("utf-8")).padToLong());

            if (tail.isEmpty()) {
                return nodes.firstEntry().getValue();
            } else {
                return nodes.get(tail.firstKey());
            }
        }
    }

    public void registerApiProvider(String api,String host,Integer port,byte[] data) throws Exception {
        String className = api;
        String url = String.format("rmi://%s:%d/%s",host,port,className).replace('/','_');
        String path = SERVER_PATH+"/"+className;
        Stat stat = framework.checkExists().forPath(path);
        if(stat == null){
            createNode(path,CreateMode.PERSISTENT);//将className注册成持久节点
        }
        String subPath = path + "/"+url;
        stat = framework.checkExists().forPath(subPath);
        if(stat == null){
            createNode(subPath,CreateMode.EPHEMERAL);
            if(data != null){
                framework.setData().forPath(subPath,data);
            }
        }else{
            throw new Exception("register url already register "+url);
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

    public void syncHashData(){
        synchronized (mqttLock) {
            List<ChildData> data = mqttServerPath.getCurrentData();
            nodes = new TreeMap<Long, String>();
            for (int i = 0; i < data.size(); i++) {
                ChildData d = data.get(i);
                String url = d.getPath().split("/")[2];

                for (int n = 0; n < 320; n++) {
                    nodes.put(hash.hashString("node-"+url+"-"+i+"-"+n, Charset.forName("utf-8")).padToLong(), url);
                }
            }
        }
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

    class MqttServiceChangeListener implements PathChildrenCacheListener{

        @Override
        public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
            syncHashData();
        }
    }


}
