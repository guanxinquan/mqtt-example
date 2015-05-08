package com.example.mqtt.cluster;

import com.example.mqtt.api.MqttListener;
import com.example.mqtt.api.MqttServer;
import com.example.mqtt.zk.IZkServer;
import com.example.mqtt.zk.ZkServerFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by guanxinquan on 15-5-8.
 */
public class MqttClusterImpl implements IMqttCluster {

    private IZkServer zkServer = ZkServerFactory.getInstance();

    private int serverVersion = -1;

    private int clientVersion = -1;

    private SortedMap<Integer,MqttServer.Client> serverMap = new ConcurrentSkipListMap<Integer, MqttServer.Client>();

    public MqttClusterImpl() {

        zkServer.fetchServerPath();



    }

    public synchronized void checkServerVersion(){
//        int zkVersion = zkServer.getServerDateVersion().get();
//        if(zkVersion != serverVersion){
//            List<ChildData> data = zkServer.fetchServerPath();
//            Set<Integer> serverSet = new HashSet<Integer>();
//
//            for(ChildData c : data){
//                MqttServer server;
//                Integer key = Integer.valueOf(new String(c.getData()));
//                server = serverMap.get(key);
//                if(server == null){
//                    server = createServer(c.getPath());
//                }
//                serverMap.put(key,server);
//                serverSet.add(key);
//            }
//
//            for(Map.Entry<Integer,MqttServer> e : serverMap.entrySet()){
//                if(!serverSet.contains(e.getKey())){
//                    closeServer(e.getValue());
//                }
//            }
//        }else{
//            return;
//        }
    }

    @Override
    public MqttServer getMqttServer(Long userId) {
        return null;
    }

    @Override
    public String getMqttServerUrl(Long userId) {
        return null;
    }

    @Override
    public MqttListener getMqttListener() {
        return null;
    }

    private ServicePair createServer(String server) throws TException {
//        TTransport transport = new TFramedTransport(new TSocket(server));
//        transport.open();
//        TProtocol protocol = new TCompactProtocol(transport);
//        MqttServer.Client client = new MqttServer.Client(protocol);
//        client.publishByClientId("1231231312", 122313, "小苹果呀小苹果", "胡说");
//
//        return pair;
        return null;
    }

    private void closeServer(MqttServer server){
    }

    class ServicePair{
        TTransport transport;
        MqttServer.Client client;
    }
}
