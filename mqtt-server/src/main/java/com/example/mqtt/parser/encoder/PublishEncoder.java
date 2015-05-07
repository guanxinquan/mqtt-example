package com.example.mqtt.parser.encoder;

import com.example.mqtt.proto.messages.AbstractMessage;
import com.example.mqtt.proto.messages.PublishMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by guanxinquan on 15-5-6.
 */
public class PublishEncoder extends DemuxEncoder<PublishMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, PublishMessage message, ByteBuf out) {
        if (message.getQos() == AbstractMessage.QOSType.RESERVED) {
            throw new IllegalArgumentException("Found a message with RESERVED Qos");
        }
        if (message.getTopicName() == null || message.getTopicName().isEmpty()) {
            throw new IllegalArgumentException("Found a message with empty or null topic name");
        }

        ByteBuf variableHeaderBuff = ctx.alloc().buffer(2);
        try {
            variableHeaderBuff.writeBytes(EncoderUtils.encodeString(message.getTopicName()));
            if (message.getQos() == AbstractMessage.QOSType.LEAST_ONE ||
                    message.getQos() == AbstractMessage.QOSType.EXACTLY_ONCE ) {
                if (message.getMessageID() == null) {
                    throw new IllegalArgumentException("Found a message with QOS 1 or 2 and not MessageID setted");
                }
                variableHeaderBuff.writeShort(message.getMessageID());
            }
            variableHeaderBuff.writeBytes(message.getPayload());
            int variableHeaderSize = variableHeaderBuff.readableBytes();

            byte flags = EncoderUtils.encodeFlags(message);

            ByteBuf buff = ctx.alloc().buffer(2 + variableHeaderSize);
            buff.writeByte(AbstractMessage.PUBLISH << 4 | flags);
            buff.writeBytes(EncoderUtils.encodeRemainingLength(variableHeaderSize));
            buff.writeBytes(variableHeaderBuff);
            out.writeBytes(buff);
        } finally {
            variableHeaderBuff.release();
        }
    }
}
