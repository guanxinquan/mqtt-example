package com.example.mqtt.parser.decoder;

import com.example.mqtt.proto.messages.AbstractMessage;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.util.AttributeMap;

import java.util.List;

/**
 * Created by guanxinquan on 15-5-5.
 */
public abstract class DemuxDecoder {

    abstract void decode(AttributeMap ctx, ByteBuf in, List<Object> out) throws Exception;

    /**
     * Decodes the first 2 bytes of the MQTT packet.
     * The first byte contain the packet operation code and the flags,
     * the second byte and more contains the overall packet length.
     */
    protected boolean decodeCommonHeader(AbstractMessage message, ByteBuf in) {
        return genericDecodeCommonHeader(message, null, in);
    }

    /**
     * Do the same as the @see#decodeCommonHeader but having a strong validation on the flags values
     */
    protected boolean decodeCommonHeader(AbstractMessage message, int expectedFlags, ByteBuf in) {
        return genericDecodeCommonHeader(message, expectedFlags, in);
    }


    private boolean genericDecodeCommonHeader(AbstractMessage message, Integer expectedFlagsOpt, ByteBuf in) {
        //Common decoding part
        if (in.readableBytes() < 2) {
            return false;
        }
        byte h1 = in.readByte();
        byte messageType = (byte) ((h1 & 0x00F0) >> 4);

        byte flags = (byte) (h1 & 0x0F);
        if (expectedFlagsOpt != null) {
            int expectedFlags = expectedFlagsOpt;
            if ((byte) expectedFlags != flags) {
                String hexExpected = Integer.toHexString(expectedFlags);
                String hexReceived = Integer.toHexString(flags);
                throw new CorruptedFrameException(String.format("Received a message with fixed header flags (%s) != expected (%s)", hexReceived, hexExpected));
            }
        }

        boolean dupFlag = ((byte) ((h1 & 0x0008) >> 3) == 1);
        byte qosLevel = (byte) ((h1 & 0x0006) >> 1);
        boolean retainFlag = ((byte) (h1 & 0x0001) == 1);
        int remainingLength = DecoderUtils.decodeRemainingLenght(in);
        if (remainingLength == -1) {
            return false;
        }

        message.setMessageType(messageType);
        message.setDupFlag(dupFlag);
        message.setQos(AbstractMessage.QOSType.values()[qosLevel]);
        message.setRetainFlag(retainFlag);
        message.setRemainingLength(remainingLength);
        return true;
    }

}
