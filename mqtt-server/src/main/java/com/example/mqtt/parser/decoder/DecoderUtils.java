package com.example.mqtt.parser.decoder;

import com.example.mqtt.proto.messages.AbstractMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.util.Attribute;
import io.netty.util.AttributeMap;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

/**
 * Created by guanxinquan on 15-5-5.
 */
public class DecoderUtils {
    public static final int MAX_LENGTH_LIMIT = 268435455;

    public static final byte VERSION_3_1 = 3;
    public static final byte VERSION_3_1_1 = 4;

    static byte readMessageType(ByteBuf in) {
        byte h1 = in.readByte();
        byte messageType = (byte) ((h1 & 0x00F0) >> 4);
        return messageType;
    }

    static boolean checkHeaderAvailability(ByteBuf in) {
        if (in.readableBytes() < 1) {
            return false;
        }
        //byte h1 = in.get();
        //byte messageType = (byte) ((h1 & 0x00F0) >> 4);
        in.skipBytes(1); //skip the messageType byte

        int remainingLength = DecoderUtils.decodeRemainingLenght(in);
        if (remainingLength == -1) {
            return false;
        }

        //check remaining length
        if (in.readableBytes() < remainingLength) {
            return false;
        }

        //return messageType == type ? MessageDecoderResult.OK : MessageDecoderResult.NOT_OK;
        return true;
    }

    /**
     * Decode the variable remaining length as defined in MQTT v3.1 specification
     * (section 2.1).
     *
     * @return the decoded length or -1 if needed more data to decode the length field.
     */
    static int decodeRemainingLenght(ByteBuf in) {
        int multiplier = 1;
        int value = 0;
        byte digit;
        do {
            if (in.readableBytes() < 1) {
                return -1;
            }
            digit = in.readByte();
            value += (digit & 0x7F) * multiplier;
            multiplier *= 128;
        } while ((digit & 0x80) != 0);
        return value;
    }

    /**
     * Encode the value in the format defined in specification as variable length
     * array.
     *
     * @throws IllegalArgumentException if the value is not in the specification bounds
     *  [0..268435455].
     */
    static ByteBuf encodeRemainingLength(int value) throws CorruptedFrameException {
        if (value > MAX_LENGTH_LIMIT || value < 0) {
            throw new CorruptedFrameException("Value should in range 0.." + MAX_LENGTH_LIMIT + " found " + value);
        }

        ByteBuf encoded = Unpooled.buffer(4);
        byte digit;
        do {
            digit = (byte) (value % 128);
            value = value / 128;
            // if there are more digits to encode, set the top bit of this digit
            if (value > 0) {
                digit = (byte) (digit | 0x80);
            }
            encoded.writeByte(digit);
        } while (value > 0);
        return encoded;
    }

    /**
     * Load a string from the given buffer, reading first the two bytes of len
     * and then the UTF-8 bytes of the string.
     *
     * @return the decoded string or null if NEED_DATA
     */
    static String decodeString(ByteBuf in) throws UnsupportedEncodingException {
        if (in.readableBytes() < 2) {
            return null;
        }
        //int strLen = Utils.readWord(in);
        int strLen = in.readUnsignedShort();
        if (in.readableBytes() < strLen) {
            return null;
        }
        byte[] strRaw = new byte[strLen];
        in.readBytes(strRaw);

        return new String(strRaw, "UTF-8");
    }


    /**
     * Return the IoBuffer with string encoded as MSB, LSB and UTF-8 encoded
     * string content.
     */
    static ByteBuf encodeString(String str) {
        ByteBuf out = Unpooled.buffer(2);
        byte[] raw;
        try {
            raw = str.getBytes("UTF-8");
            //NB every Java platform has got UTF-8 encoding by default, so this
            //exception are never raised.
        } catch (UnsupportedEncodingException ex) {
            LoggerFactory.getLogger(DecoderUtils.class).error(null, ex);
            return null;
        }
        //Utils.writeWord(out, raw.length);
        out.writeShort(raw.length);
        out.writeBytes(raw);
        return out;
    }

    /**
     * Return the number of bytes to encode the given remaining length value
     */
    static int numBytesToEncode(int len) {
        if (0 <= len && len <= 127) return 1;
        if (128 <= len && len <= 16383) return 2;
        if (16384 <= len && len <= 2097151) return 3;
        if (2097152 <= len && len <= 268435455) return 4;
        throw new IllegalArgumentException("value shoul be in the range [0..268435455]");
    }

    static byte encodeFlags(AbstractMessage message) {
        byte flags = 0;
        if (message.isDupFlag()) {
            flags |= 0x08;
        }
        if (message.isRetainFlag()) {
            flags |= 0x01;
        }

        flags |= ((message.getQos().ordinal() & 0x03) << 1);
        return flags;
    }

    static boolean isMQTT3_1_1(AttributeMap attrsMap) {
        Attribute<Integer> versionAttr = attrsMap.attr(MQTTDecoder.PROTOCOL_VERSION);
        Integer protocolVersion = versionAttr.get();
        if (protocolVersion == null) {
            return true;
        }
        return protocolVersion == VERSION_3_1_1;
    }

    public static String msgType2String(int type) {
        switch (type) {
            case AbstractMessage.CONNECT: return "CONNECT";
            case AbstractMessage.CONNACK: return "CONNACK";
            case AbstractMessage.PUBLISH: return "PUBLISH";
            case AbstractMessage.PUBACK: return "PUBACK";
            case AbstractMessage.PUBREC: return "PUBREC";
            case AbstractMessage.PUBREL: return "PUBREL";
            case AbstractMessage.PUBCOMP: return "PUBCOMP";
            case AbstractMessage.SUBSCRIBE: return "SUBSCRIBE";
            case AbstractMessage.SUBACK: return "SUBACK";
            case AbstractMessage.UNSUBSCRIBE: return "UNSUBSCRIBE";
            case AbstractMessage.UNSUBACK: return "UNSUBACK";
            case AbstractMessage.PINGREQ: return "PINGREQ";
            case AbstractMessage.PINGRESP: return "PINGRESP";
            case AbstractMessage.DISCONNECT: return "DISCONNECT";
            default: throw  new RuntimeException("Can't decode message type " + type);
        }
    }
}