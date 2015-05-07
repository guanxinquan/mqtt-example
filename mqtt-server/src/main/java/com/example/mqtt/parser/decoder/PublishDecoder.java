package com.example.mqtt.parser.decoder;

import com.example.mqtt.proto.messages.PublishMessage;
import com.example.mqtt.proto.messages.AbstractMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.util.AttributeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by guanxinquan on 15-5-6.
 */
public class PublishDecoder extends DemuxDecoder {

    private static Logger logger = LoggerFactory.getLogger(PublishDecoder.class);

    @Override
    void decode(AttributeMap ctx, ByteBuf in, List<Object> out) throws Exception {
        logger.info("decode invoked with buffer {}", in);
        in.resetReaderIndex();
        int startPos = in.readerIndex();

        //Common decoding part
        PublishMessage message = new PublishMessage();
        if (!decodeCommonHeader(message, in)) {
            logger.info("decode ask for more data after {}", in);
            in.resetReaderIndex();
            throw new CorruptedFrameException("Received a PUBLISH but data not enough");
        }

        if (DecoderUtils.isMQTT3_1_1(ctx)) {
            if (message.getQos() == AbstractMessage.QOSType.MOST_ONE && message.isDupFlag()) {
                //bad protocol, if QoS=0 => DUP = 0
                throw new CorruptedFrameException("Received a PUBLISH with QoS=0 & DUP = 1, MQTT 3.1.1 violation");
            }

            if (message.getQos() == AbstractMessage.QOSType.RESERVED) {
                throw new CorruptedFrameException("Received a PUBLISH with QoS flags setted 10 b11, MQTT 3.1.1 violation");
            }
        }

        int remainingLength = message.getRemainingLength();

        //Topic name
        String topic = DecoderUtils.decodeString(in);
        if (topic == null) {
            in.resetReaderIndex();
            throw new CorruptedFrameException("Received a PUBLISH with topic containing null");
        }
        if (topic.contains("+") || topic.contains("#")) {
            throw new CorruptedFrameException("Received a PUBLISH with topic containing wild card chars, topic: " + topic);
        }

        message.setTopicName(topic);

        if (message.getQos() == AbstractMessage.QOSType.LEAST_ONE ||
                message.getQos() == AbstractMessage.QOSType.EXACTLY_ONCE) {
            message.setMessageID(in.readUnsignedShort());
        }
        int stopPos = in.readerIndex();

        //read the payload
        int payloadSize = remainingLength - (stopPos - startPos - 2) + (DecoderUtils.numBytesToEncode(remainingLength) - 1);
        if (in.readableBytes() < payloadSize) {
            in.resetReaderIndex();
            throw new CorruptedFrameException("Received a PUBLISH but data not enough");
        }
//        byte[] b = new byte[payloadSize];
        ByteBuf bb = Unpooled.buffer(payloadSize);
        in.readBytes(bb);
        message.setPayload(bb.nioBuffer());

        out.add(message);
    }
}
