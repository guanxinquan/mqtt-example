package com.example.mqtt.proto.messages;

/**
 * Created by guanxinquan on 15-5-5.
 */
public class PingRespMessage extends ZeroLengthMessage{
    public PingRespMessage() {
        messageType = PINGRESP;
    }
}
