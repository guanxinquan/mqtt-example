package org.eclipse.paho.client.mqttv3;

import com.example.mqtt.zk.IZkServer;
import com.example.mqtt.zk.ZkServerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by guanxinquan on 15-5-18.
 */
public class MqttClientTest {


    private MqttClient client;

    private MqttConnectOptions conOpt = new MqttConnectOptions();

    private static final Logger logger = LoggerFactory.getLogger(MqttClientTest.class);

    private static String topic ="1";

    private static boolean isOk = false;

    private String getBrokerUrl(String userName){
        return String.format("tcp://%s:%s","127.0.0.1",6379);
    }

    public boolean conn(){
        try {
            client.connect(conOpt);
            return true;
        } catch (MqttException e) {
            logger.info("mqtt server connect error !",e);
            return false;
        }
    }

    public MqttClient getClient() {
        return client;
    }

    public void setClient(MqttClient client) {
        this.client = client;
    }

    public MqttClientTest(String userId,String password,String clientId) {

        conOpt.setCleanSession(true);
        conOpt.setKeepAliveInterval(100);

        conOpt.setUserName(clientId);
        conOpt.setPassword(clientId.toCharArray());
        conOpt.setWill("pub,1","".getBytes(),0,false);
        try {
            String url = getBrokerUrl(userId);
            logger.info("mqtt url is {} clientId is {}",url,clientId);
            client = new MqttClient(url,clientId);
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    logger.info("connect lost ",cause);
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    System.out.println("message arrival");
                    byte[] bytes = message.getPayload();
                    ByteInputStream inputStream = new ByteInputStream(bytes,bytes.length);

                    List<String> payloadMsg = parserPayload(inputStream,bytes.length);

                    ObjectMapper mapper = new ObjectMapper();
                    logger.info("message arrival topic {} payload {}", topic, mapper.writeValueAsString(payloadMsg));
                    client.aClient.publish(topic,new byte[]{},0,false);
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
                        ret.add(new String(msg,Charset.forName("utf-8")));
                        currentSize += 4;
                        currentSize += bs;
                    }
                    return ret;
                }


                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }

            });
            client.setTimeToWait(1000l);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws JsonProcessingException, MqttException, InterruptedException {

        String userId = "9998";

        ObjectMapper mapper = new ObjectMapper();

        UserModel from = new UserModel(1,22222l,"张三");
        UserModel to = new UserModel(2,312311l,"李四");
        MessageContent content = new MessageContent(1,"记得下班后去吃饭");
        OneMessage oneMessage = new OneMessage(1,from,to,content);

        //byte[] payload =  //= mapper.writeValueAsString(oneMessage).getBytes(Charset.forName("utf-8"));

        MqttClientTest test = new MqttClientTest(userId,userId,userId);
        boolean conn = test.conn();
        MqttClient client = test.client;
        if(!conn){
            return;
        }

        long s = System.currentTimeMillis();
        for(int i = 0 ; i < 3000 ; i++){
            byte[] payload = String.valueOf("k"+i).getBytes();
            client.publish("pub",payload,1,false);
//            while(true) {
//                if(isOk) {
//                    client.publish(topic, topic.getBytes(), 0, false);
//                    isOk = false;
//                    break;
//                }
                Thread.sleep(10l);
//            }
            //client.publish("8","8".getBytes(),0,false);
        }
        Thread.sleep(1000000l);
        logger.info("user time :{}",System.currentTimeMillis() - s);
        client.disconnect();
    }
}
