package org.eclipse.paho.client.mqttv3;

import com.example.mqtt.zk.IZkServer;
import com.example.mqtt.zk.ZkServerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

/**
 * Created by guanxinquan on 15-5-18.
 */
public class MqttClientTest {

    private IZkServer zkServer = ZkServerFactory.getInstance();

    private MqttClient client;

    private MqttConnectOptions conOpt = new MqttConnectOptions();

    private static final Logger logger = LoggerFactory.getLogger(MqttClientTest.class);

    private String getBrokerUrl(String userName){
        String server = zkServer.fetchServer(userName);
        String[] splits =  server.split(":");
        return String.format("tcp://%s:%s",splits[0],splits[1]);
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
                    logger.info("message arrival topic {} payload {}",topic,new String(message.getPayload()));
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
        System.setProperty("zk","localhost:2181");

        String userId = "9998";

        ObjectMapper mapper = new ObjectMapper();

        UserModel from = new UserModel(1,22222l,"张三");
        UserModel to = new UserModel(2,312311l,"李四");
        MessageContent content = new MessageContent(1,"记得下班后去吃饭");
        OneMessage oneMessage = new OneMessage(1,from,to,content);

        byte[] payload = mapper.writeValueAsString(oneMessage).getBytes(Charset.forName("utf-8"));

        MqttClientTest test = new MqttClientTest(userId,userId,userId);
        boolean conn = test.conn();
        MqttClient client = test.client;
        if(!conn){
            return;
        }

        long s = System.currentTimeMillis();
        for(int i = 0 ; i < 10000 ; i++){
            client.publish("999",payload,1,false);
            Thread.sleep(100l);
        }
        Thread.sleep(10000l);
        logger.info("user time :{}",System.currentTimeMillis() - s);
        client.disconnect();
    }
}
