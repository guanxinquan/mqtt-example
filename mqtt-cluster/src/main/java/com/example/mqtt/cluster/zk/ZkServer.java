package com.example.mqtt.cluster.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by guanxinquan on 15-5-7.
 */
public class ZkServer {

    public static final int DEFAULT_RETRY_TIMES = 500000;
    public static final int DEFAULT_RETRY_DURATION = 10000;
    public static final int DEFAULT_CONNECTION_TIME_OUT = 30000;

    private static final String NAME_SPACE = "/mqtt/cluster/";

    private static final Logger logger = LoggerFactory.getLogger(ZkServer.class);

    private CuratorFramework framework;

    private ConcurrentSkipListSet<String> watchers = new ConcurrentSkipListSet<String>();

    private String hosts;



    public ZkServer(String hosts) {
        this.hosts = hosts;
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

    }
    /**
     * 添加对session过期的事件处理
     * @param path 节点名字
     * @param watcherType 监听事件的类型
     * @param watcher 监听实现
     */
    public void addReconnectionWatcher(final String path,final ZookeeperWatcherType watcherType,final CuratorWatcher watcher){
        synchronized (this) {
            if(!watchers.contains(path))//不要添加重复的监听事件
            {
                watchers.add(path);
                logger.info("add new watcher " + watcher);
                framework.getConnectionStateListenable().addListener(new ConnectionStateListener() {
                    @Override
                    public void stateChanged(CuratorFramework client, ConnectionState newState) {
                        System.out.println(newState);
                        if(newState == ConnectionState.LOST){//处理session过期
                            try{
                                if(watcherType == ZookeeperWatcherType.EXITS){
                                    framework.checkExists().usingWatcher(watcher).forPath(path);
                                }else if(watcherType == ZookeeperWatcherType.GET_CHILDREN){
                                    framework.getChildren().usingWatcher(watcher).forPath(path);
                                }else if(watcherType == ZookeeperWatcherType.GET_DATA){
                                    logger.info("zk server connect lost and session already time out, now rebuild session and add watcher");
                                    framework.getData().usingWatcher(watcher).forPath(path);
                                }else if(watcherType == ZookeeperWatcherType.CREATE_ON_NO_EXITS){
                                    //ephemeral类型的节点session过期了，需要重新创建节点，并且注册监听事件，之后监听事件中，
                                    //会处理create事件，将路径值恢复到先前状态
                                    Stat stat = framework.checkExists().usingWatcher(watcher).forPath(path);
                                    if(stat == null){
                                        logger.error("to create");
                                        framework.create()
                                                .creatingParentsIfNeeded()
                                                .withMode(CreateMode.EPHEMERAL)
                                                .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                                                .forPath(path);
                                    }
                                }
                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }
    }


    public enum ZookeeperWatcherType{
        GET_DATA,GET_CHILDREN,EXITS,CREATE_ON_NO_EXITS
    }
}
