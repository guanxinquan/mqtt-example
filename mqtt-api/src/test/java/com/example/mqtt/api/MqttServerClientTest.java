package com.example.mqtt.api;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

/**
 * Created by guanxinquan on 15-5-8.
 */
public class MqttServerClientTest {

    public static void main(String[] args) throws TException {

        MqttServerClientTest test = new MqttServerClientTest();
        test.invoke();
    }

    public void invoke() throws TException {
        TTransport transport = new TFramedTransport(new TSocket("localhost",12306));
        transport.open();
        TProtocol protocol = new TCompactProtocol(transport);
        MqttServer.Client client = new MqttServer.Client(protocol);
        client.publishByClientId("1231231312",122313,"小苹果呀小苹果","胡说");
        transport.close();
    }

}
