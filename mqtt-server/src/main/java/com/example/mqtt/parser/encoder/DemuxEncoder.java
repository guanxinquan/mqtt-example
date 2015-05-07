package com.example.mqtt.parser.encoder;

import com.example.mqtt.proto.messages.AbstractMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by guanxinquan on 15-5-5.
 */
public abstract class DemuxEncoder<T extends AbstractMessage> {
    abstract protected void encode(ChannelHandlerContext chc, T msg, ByteBuf bb);
}
