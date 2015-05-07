package com.example.mqtt.proto.messages;

import java.nio.ByteBuffer;

/**
 * Created by guanxinquan on 15-5-6.
 */
public class PublishMessage extends MessageIDMessage{

    private String topicName;

    private ByteBuffer payload;

    public PublishMessage() {
        messageType = PUBLISH;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public ByteBuffer getPayload() {
        return payload;
    }

    public void setPayload(ByteBuffer payload) {
        this.payload = payload;
    }

    public void reset(){this.payload.rewind();}
}
