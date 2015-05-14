package com.example.mqtt.config;

/**
 * Created by guanxinquan on 15-5-14.
 */
public class MqttConfig {

    private static String host;

    private static String mqttPort;

    private static String rmiPort;

    static{
        host = System.getProperty("host");
        if(host == null)
            host = "localhost";

        mqttPort = System.getProperty("mqttPort");
        if(mqttPort == null)
            mqttPort = "1883";

        rmiPort = System.getProperty("rmiPort");
        if(rmiPort == null)
            rmiPort = "1088";

    }

    public static String getHost() {
        return host;
    }

    public static void setHost(String host) {
        MqttConfig.host = host;
    }

    public static String getMqttPort() {
        return mqttPort;
    }

    public static void setMqttPort(String mqttPort) {
        MqttConfig.mqttPort = mqttPort;
    }

    public static String getRmiPort() {
        return rmiPort;
    }

    public static void setRmiPort(String rmiPort) {
        MqttConfig.rmiPort = rmiPort;
    }

    public static String getMqttUrl(){
        return host + ":"+mqttPort;
    }
}
