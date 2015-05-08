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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by guanxinquan on 15-5-7.
 */
public class ZkServer implements IZkServer{

    public static final int DEFAULT_RETRY_TIMES = 500000;
    public static final int DEFAULT_RETRY_DURATION = 10000;
    public static final int DEFAULT_CONNECTION_TIME_OUT = 30000;



    private static final String NAME_SPACE = "mqtt/cluster";

    private static final String SERVER_PATH = "/server";

    private static final String CLIENT_PATH = "/client";

    private static final Logger logger = LoggerFactory.getLogger(ZkServer.class);

    private CuratorFramework framework;

    private String hosts;

    private PathChildrenCache serverCache;

    private PathChildrenCache clientCache;

    private AtomicInteger serverDataVersion = new AtomicInteger();

    private AtomicInteger clientDataVersion = new AtomicInteger();


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

        serverCache = new PathChildrenCache(framework,SERVER_PATH,true);
        clientCache = new PathChildrenCache(framework,CLIENT_PATH,true);

        serverCache.getListenable().addListener(new ServerChangeListener());
        clientCache.getListenable().addListener(new ClientChangeListener());

        try {
            serverCache.start();
            clientCache.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public List<ChildData> fetchServerPath(){
        return serverCache.getCurrentData();
    }

    @Override
    public AtomicInteger getServerDateVersion() {
        return serverDataVersion;
    }

    @Override
    public AtomicInteger getClientDateVersion() {
        return clientDataVersion;
    }

    @Override
    public void registerServerPath(String path, byte[] data) {

        path = SERVER_PATH + "/" + path;
        registerPath(path,data);
    }

    @Override
    public void registerClientPath(String path, byte[] data) {
        path = CLIENT_PATH + "/" + path;
        registerPath(path,data);
    }

    private void registerPath(String path,byte[] data){
        CreateMode mode = CreateMode.EPHEMERAL;
        try {
            Stat stat = framework.checkExists().forPath(path);
            if(stat == null){
                framework.create().
                        creatingParentsIfNeeded().
                        withMode(mode).
                        withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE).
                        forPath(path);
                framework.setData().forPath(path,data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public List<ChildData> fetchClientPath(){
        return clientCache.getCurrentData();
    }

    @Override
    public void close() throws IOException {
        try {
            serverCache.close();
            clientCache.close();
        }catch (Exception e){

        }finally {
            framework.close();
        }

    }

    class ClientChangeListener implements PathChildrenCacheListener {

        @Override
        public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
            logger.info("client children path change {}",pathChildrenCacheEvent.getData().getPath());
            clientDataVersion.incrementAndGet();
        }
    }

    class ServerChangeListener implements PathChildrenCacheListener{

        @Override
        public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
            logger.info("server children path change {}",pathChildrenCacheEvent.getData().getPath());
            serverDataVersion.incrementAndGet();
        }
    }

}
