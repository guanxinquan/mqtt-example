package com.example.mqtt.proto.messages;

/**
 * Created by guanxinquan on 15-5-6.
 */
public abstract class MessageIDMessage extends AbstractMessage {

    private Integer messageID;


    public Integer getMessageID() {
        return messageID;
    }

    public void setMessageID(Integer messageID) {
        this.messageID = messageID;
    }
}
