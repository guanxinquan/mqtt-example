package com.example.mqtt.parser.encoder;

import com.example.mqtt.proto.messages.PingRespMessage;
import com.example.mqtt.proto.messages.AbstractMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by guanxinquan on 15-5-5.
 */
public class PingReqEncoder extends DemuxEncoder<PingRespMessage>{
    @Override
    protected void encode(ChannelHandlerContext chc, PingRespMessage msg, ByteBuf out) {
        out.writeByte(AbstractMessage.PINGREQ << 4).writeByte(0);
    }
}
