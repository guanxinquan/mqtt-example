package com.example.mqtt.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by guanxinquan on 15-5-20.
 * 用于统计需求
 *
 * 超时统计，用户客户端超时的时候统计。
 * 连接统计，用户connect的时候统计。
 * 消息统计，用户发送，接收消息的统计。
 * 重发统计
 */
public class StatLogger {

    private static final Logger logger = LoggerFactory.getLogger(StatLogger.class);

    public static final String TIME_OUT = "TOUT";

    public static final String CONNECT = "CONN";

    public static final String PUB_UP = "PUBU";

    public static final String PUB_DOWN = "PUBD";

    public static final String PUB_RETRY = "PUBR";

    public static final void logger(String type,String userName,String clientId,String others){
        logger.info("{}||{}||{}||{}",type,userName,clientId,others);

    }


}
