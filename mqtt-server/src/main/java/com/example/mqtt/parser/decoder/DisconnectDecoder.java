package com.example.mqtt.parser.decoder;

import com.example.mqtt.proto.messages.DisconnectMessage;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.util.AttributeMap;

import java.util.List;

/**
 * Created by guanxinquan on 15-5-6.
 */
public class DisconnectDecoder extends DemuxDecoder{
    @Override
    void decode(AttributeMap ctx, ByteBuf in, List<Object> out) throws Exception {
        in.resetReaderIndex();
        DisconnectMessage message = new DisconnectMessage();
        if (!decodeCommonHeader(message, 0x00, in)) {
            in.resetReaderIndex();
            throw new CorruptedFrameException("Received Ping request but message parser error");
        }
        out.add(message);
    }
}
