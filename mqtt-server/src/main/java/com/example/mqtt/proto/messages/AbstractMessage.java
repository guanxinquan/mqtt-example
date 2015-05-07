package com.example.mqtt.proto.messages;

/**
 * Created by guanxinquan on 15-5-5.
 */
public abstract class AbstractMessage {

    public static final byte CONNECT = 1; // Client request to connect to Server
    public static final byte CONNACK = 2; // Connect Acknowledgment
    public static final byte PUBLISH = 3; // Publish message
    public static final byte PUBACK = 4; // Publish Acknowledgment
    public static final byte PUBREC = 5; //Publish Received (assured delivery part 1)
    public static final byte PUBREL = 6; // Publish Release (assured delivery part 2)
    public static final byte PUBCOMP = 7; //Publish Complete (assured delivery part 3)
    public static final byte SUBSCRIBE = 8; //Client Subscribe request
    public static final byte SUBACK = 9; // Subscribe Acknowledgment
    public static final byte UNSUBSCRIBE = 10; //Client Unsubscribe request
    public static final byte UNSUBACK = 11; // Unsubscribe Acknowledgment
    public static final byte PINGREQ = 12; //PING Request
    public static final byte PINGRESP = 13; //PING Response
    public static final byte DISCONNECT = 14; //Client is Disconnecting

    public static enum QOSType {
        MOST_ONE, LEAST_ONE, EXACTLY_ONCE, RESERVED;

        public static String formatQoS(QOSType qos) {
            return String.format("%d - %s", qos.ordinal(), qos.name());
        }
    }
    //type
    protected boolean dupFlag;
    protected QOSType qos;
    protected boolean retainFlag;
    protected int remainingLength;
    protected byte messageType;

    public byte getMessageType() {
        return messageType;
    }

    public void setMessageType(byte messageType) {
        this.messageType = messageType;
    }

    public boolean isDupFlag() {
        return dupFlag;
    }

    public void setDupFlag(boolean dupFlag) {
        this.dupFlag = dupFlag;
    }

    public QOSType getQos() {
        return qos;
    }

    public void setQos(QOSType qos) {
        this.qos = qos;
    }

    public boolean isRetainFlag() {
        return retainFlag;
    }

    public void setRetainFlag(boolean retainFlag) {
        this.retainFlag = retainFlag;
    }

    /**
     * TOBE used only internally
     */
    public int getRemainingLength() {
        return remainingLength;
    }

    /**
     * TOBE used only internally
     */
    public void setRemainingLength(int remainingLength) {
        this.remainingLength = remainingLength;
    }


}
