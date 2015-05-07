package com.example.mqtt.proto.messages;

/**
 * Created by guanxinquan on 15-5-6.
 */
public class PingReqMessage extends ZeroLengthMessage{
    public PingReqMessage() {
        messageType = PINGREQ;
    }
}
