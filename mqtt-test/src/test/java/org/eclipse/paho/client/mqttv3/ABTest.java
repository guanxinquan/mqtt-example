package org.eclipse.paho.client.mqttv3;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by guanxinquan on 15-6-11.
 */
public class ABTest{

    private String userId;

    public ABTest(String userId) {
        this.userId = userId;
    }

    private String getBrokerUrl(String userName){
        return String.format("tcp://%s:%s","127.0.0.1",6379);
    }

    public  MqttClient getClient(){


        MqttConnectOptions conOpt = new MqttConnectOptions();
        conOpt.setCleanSession(true);
        conOpt.setKeepAliveInterval(100);

        conOpt.setUserName(userId);
        conOpt.setPassword(userId.toCharArray());
        conOpt.setWill("pub,1","".getBytes(),0,false);

        final Logger logger = LoggerFactory.getLogger(ABTest.class+userId);
        final MqttClient client;
        try {
            String url = getBrokerUrl(userId);
            logger.info("mqtt url is {} clientId is {}", url, userId);
            client = new MqttClient(url, userId);
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    logger.info("connect lost ", cause);
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    //logger.info("message arrival clientId {} topic {} payload {}",userId, topic, new String(message.getPayload()));
                    byte[] bytes = message.getPayload();
                    ByteInputStream inputStream = new ByteInputStream(bytes,bytes.length);

                    List<String> payloadMsg = parserPayload(inputStream,bytes.length);

                    ObjectMapper mapper = new ObjectMapper();
                    logger.info("message arrival topic {} payload {}", topic, mapper.writeValueAsString(payloadMsg));

                    client.aClient.publish(topic,new byte[]{},0,false);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }


                private List<String> parserPayload(ByteInputStream inputStream,int size) throws IOException {
                    int currentSize = 0;
                    byte[] bufSize = new byte[4];
                    List<String> ret = new ArrayList<String>();
                    while(currentSize < size){
                        inputStream.read(bufSize);
                        ByteBuffer wrap = ByteBuffer.wrap(bufSize);
                        int bs = wrap.getInt();
                        byte[] msg = new byte[bs];
                        inputStream.read(msg);
                        ret.add(new String(msg, Charset.forName("utf-8")));
                        currentSize += 4;
                        currentSize += bs;
                    }
                    return ret;
                }

            });




            client.setTimeToWait(1000l);
            client.connect(conOpt);
            return client;

        }catch (Exception e){

        }
        return null;
    }

    private static Executor executor = Executors.newFixedThreadPool(500);

    public static void main(String args[]) throws InterruptedException {
        for(int i=1 ; i < 400 ; i++){
            Thread.sleep(10l);
            executor.execute(new RunTest(i+""));
        }
    }

}

class RunTest implements Runnable{

    private String clientId;

    public RunTest(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public void run() {
        ABTest abTest = new ABTest(clientId);
        MqttClient client = abTest.getClient();
        for(int i = 0 ; i < 100; i++){
            byte[] payload = String.valueOf(clientId+"-"+i).getBytes();
            try {
                client.publish("pub",payload,1,false);
                Thread.sleep(5000l);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}