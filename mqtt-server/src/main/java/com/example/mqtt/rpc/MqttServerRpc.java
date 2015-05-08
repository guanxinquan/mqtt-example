package com.example.mqtt.rpc;

import com.example.mqtt.api.MqttServer;
import com.example.mqtt.zk.IZkServer;
import com.example.mqtt.zk.ZkServerFactory;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by guanxinquan on 15-5-8.
 * 这是一个rpc服务提供者
 *
 */
public class MqttServerRpc implements IMqttServerRpc{

    private IZkServer zkServer = ZkServerFactory.getInstance();
    private TServer server ;
    private static final Logger logger = LoggerFactory.getLogger(MqttServerRpc.class);


    @Override
    public void startUp(String registerHosts, String host, Integer port,Integer id) throws Exception {
        TNonblockingServerTransport serverTransport = new TNonblockingServerSocket(port);
        TTransportFactory transportFactory = new TFramedTransport.Factory();
        TProtocolFactory protocolFactory = new TCompactProtocol.Factory();
        TProcessor MqttServerProcess = new MqttServer.Processor(new MqttServerImpl());

        server = new TThreadedSelectorServer(
                new TThreadedSelectorServer.Args(serverTransport)
                        .protocolFactory(protocolFactory)
                        .transportFactory(transportFactory)
                        .processor(MqttServerProcess)
        );

        logger.info("start rcp server at register {} host {} port {} id {}", registerHosts, host, port, id);
        server.serve();
        zkServer.registerServerPath(host+":"+port,id.toString().getBytes(Charset.forName("utf-8")));
    }

    @Override
    public void close() throws IOException {
        server.stop();
    }
}
