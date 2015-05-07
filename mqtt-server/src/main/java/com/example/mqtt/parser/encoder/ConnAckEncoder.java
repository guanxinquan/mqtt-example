package com.example.mqtt.parser.encoder;

import com.example.mqtt.proto.messages.AbstractMessage;
import com.example.mqtt.proto.messages.ConnAckMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by guanxinquan on 15-5-6.
 */
public class ConnAckEncoder extends DemuxEncoder<ConnAckMessage> {
    @Override
    protected void encode(ChannelHandlerContext chc, ConnAckMessage msg, ByteBuf out) {
        out.writeByte(AbstractMessage.CONNACK << 4);
        out.writeBytes(EncoderUtils.encodeRemainingLength(2));
        out.writeByte(msg.isSessionPresent() ? 0x01 : 0x00);
        out.writeByte(msg.getReturnCode());
    }
}
