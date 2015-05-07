package com.example.mqtt.parser.decoder;

import com.example.mqtt.proto.messages.AbstractMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by guanxinquan on 15-5-5.
 */
public class MQTTDecoder extends ByteToMessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(MQTTDecoder.class);

    public static final AttributeKey<Integer> PROTOCOL_VERSION = AttributeKey.newInstance("version");

    private final Map<Byte, DemuxDecoder> decoderMap = new HashMap<Byte, DemuxDecoder>();

    public MQTTDecoder() {
        decoderMap.put(AbstractMessage.CONNECT,new ConnectDecoder());
        decoderMap.put(AbstractMessage.PUBLISH,new PublishDecoder());
        decoderMap.put(AbstractMessage.PUBACK,new PubAckDecoder());
        decoderMap.put(AbstractMessage.PINGREQ,new PingReqDecoder());
        decoderMap.put(AbstractMessage.DISCONNECT,new DisconnectDecoder());
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        in.markReaderIndex();
        if (!DecoderUtils.checkHeaderAvailability(in)) {//这里判断后，会读取完整的一条消息
            in.resetReaderIndex();
            return;
        }
        in.resetReaderIndex();

        byte messageType = DecoderUtils.readMessageType(in);

        DemuxDecoder decoder = decoderMap.get(messageType);
        if (decoder == null) {
            throw new CorruptedFrameException("Can't find any suitable decoder for message type: " + messageType);
        }
        decoder.decode(ctx, in, out);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("mqtt decoder error ",cause);
        ctx.close();
    }
}
