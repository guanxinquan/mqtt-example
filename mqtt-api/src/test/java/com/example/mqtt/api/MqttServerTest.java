package com.example.mqtt.api;

import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.*;

/**
 * Created by guanxinquan on 15-5-8.
 */
public class MqttServerTest {

    public static void main(String[] args) throws TTransportException, InterruptedException {
        MqttServerTest test = new MqttServerTest();
        test.startServer();

        Thread.sleep(100000000l);
    }


    public void startServer() throws TTransportException {
        TNonblockingServerTransport serverTransport = new TNonblockingServerSocket(12306);
        TTransportFactory transportFactory = new TFramedTransport.Factory();
        TProtocolFactory protocolFactory = new TCompactProtocol.Factory();
        TProcessor MqttServerProcess = new MqttServer.Processor(new MqttServerImpl());

        TServer server = new TThreadedSelectorServer(
                new TThreadedSelectorServer.Args(serverTransport)
                        .protocolFactory(protocolFactory)
                        .transportFactory(transportFactory)
                        .processor(MqttServerProcess)
        );

        System.out.println("Start server on port 7911...");
        server.serve();
    }


}
