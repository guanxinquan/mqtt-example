package com.example.mqtt.proto.messages;

/**
 * Created by guanxinquan on 15-5-6.
 */
public class ConnAckMessage extends AbstractMessage {
    public static final byte CONNECTION_ACCEPTED = 0x00;
    public static final byte UNNACEPTABLE_PROTOCOL_VERSION = 0x01;
    public static final byte IDENTIFIER_REJECTED = 0x02;
    public static final byte SERVER_UNAVAILABLE = 0x03;
    public static final byte BAD_USERNAME_OR_PASSWORD = 0x04;
    public static final byte NOT_AUTHORIZED = 0x05;

    private byte returnCode;
    private boolean sessionPresent;

    public ConnAckMessage() {
        messageType = CONNACK;
    }

    public byte getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(byte returnCode) {
        this.returnCode = returnCode;
    }

    public boolean isSessionPresent() {
        return this.sessionPresent;
    }

    public void setSessionPresent(boolean present) {
        this.sessionPresent = present;
    }
}
