package com.example.mqtt.parser.decoder;

import com.example.mqtt.proto.messages.MessageIDMessage;
import io.netty.buffer.ByteBuf;
import io.netty.util.AttributeMap;

import java.util.List;

/**
 * Created by guanxinquan on 15-5-6.
 */
public abstract  class MessageIDDecoder extends DemuxDecoder{
    protected abstract MessageIDMessage createMessage();

    @Override
    void decode(AttributeMap ctx, ByteBuf in, List<Object> out) throws Exception {
        in.resetReaderIndex();
        //Common decoding part
        MessageIDMessage message = createMessage();
        if (!decodeCommonHeader(message, 0x00, in)) {
            in.resetReaderIndex();
            return;
        }

        //read  messageIDs
        message.setMessageID(in.readUnsignedShort());
        out.add(message);
    }
}
