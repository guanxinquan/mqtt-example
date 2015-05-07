package com.example.mqtt.parser.encoder;

import com.example.mqtt.proto.messages.AbstractMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by guanxinquan on 15-5-5.
 */
public class MQTTEncoder extends MessageToByteEncoder<AbstractMessage> {
    private Map<Byte, DemuxEncoder> encoderMap = new HashMap<Byte, DemuxEncoder>();


    public MQTTEncoder(){
        encoderMap.put(AbstractMessage.CONNACK,new ConnAckEncoder());
        encoderMap.put(AbstractMessage.PUBLISH,new PublishEncoder());
        encoderMap.put(AbstractMessage.PUBACK,new PubAckEncoder());
        encoderMap.put(AbstractMessage.PINGRESP,new PingReqEncoder());
    }



    @Override
    protected void encode(ChannelHandlerContext ctx, AbstractMessage msg, ByteBuf out) throws Exception {
        DemuxEncoder encoder = encoderMap.get(msg.getMessageType());
        if (encoder == null) {
            throw new CorruptedFrameException("Can't find any suitable decoder for message type: " + msg.getMessageType());
        }
        encoder.encode(ctx, msg, out);
    }
}
