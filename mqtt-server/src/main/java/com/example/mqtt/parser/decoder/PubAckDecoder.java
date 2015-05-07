package com.example.mqtt.parser.decoder;

import com.example.mqtt.proto.messages.MessageIDMessage;
import com.example.mqtt.proto.messages.PubAckMessage;

/**
 * Created by guanxinquan on 15-5-6.
 */
public class PubAckDecoder extends MessageIDDecoder{

    @Override
    protected MessageIDMessage createMessage() {
        return new PubAckMessage();
    }
}
