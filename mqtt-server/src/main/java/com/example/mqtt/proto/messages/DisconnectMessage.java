package com.example.mqtt.proto.messages;

/**
 * Created by guanxinquan on 15-5-6.
 */
public class DisconnectMessage extends ZeroLengthMessage{
    public DisconnectMessage() {
        messageType = DISCONNECT;
    }
}
