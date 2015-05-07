package com.example.mqtt.parser.encoder;

import com.example.mqtt.proto.messages.PubAckMessage;
import com.example.mqtt.proto.messages.AbstractMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by guanxinquan on 15-5-6.
 */
public class PubAckEncoder extends DemuxEncoder<PubAckMessage>{
    @Override
    protected void encode(ChannelHandlerContext chc, PubAckMessage msg, ByteBuf out) {
        ByteBuf buff = chc.alloc().buffer(4);
        try {
            buff.writeByte(AbstractMessage.PUBACK << 4);
            buff.writeBytes(EncoderUtils.encodeRemainingLength(2));
            buff.writeShort(msg.getMessageID());
            out.writeBytes(buff);
        } finally {
            buff.release();
        }
    }
}
