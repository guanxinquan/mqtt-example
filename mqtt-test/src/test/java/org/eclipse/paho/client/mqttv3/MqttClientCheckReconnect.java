package org.eclipse.paho.client.mqttv3;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

/**
 * Created by guanxinquan on 15-5-18.
 */
public class MqttClientCheckReconnect {

    private static final Logger logger = LoggerFactory.getLogger(MqttClientCheckReconnect.class);

    public static void main(String[] args) throws InterruptedException, MqttException {

            System.setProperty("zk", "localhost:2181");

            String userId = "9997";


            MqttClientTest test = new MqttClientTest(userId, userId, userId);
            boolean conn = test.conn();

            final MqttClient client = test.getClient();
            if (!conn) {
                return;
            }

            long s = System.currentTimeMillis();
            Runtime.getRuntime().addShutdownHook(new Thread(){
                @Override
                public void run() {
                    try {
                        logger.info("shut down hook");
                        client.disconnect();
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            });

            Thread.sleep(10000000l);


            client.disconnect();
    }

}
