package com.example.mqtt.parser.decoder;

import com.example.mqtt.proto.messages.ConnectMessage;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.AttributeMap;

import java.util.List;

/**
 * Created by guanxinquan on 15-5-5.
 */
public class ConnectDecoder extends DemuxDecoder {

    static final AttributeKey<Boolean> CONNECT_STATUS = AttributeKey.newInstance("connected");


    @Override
    void decode(AttributeMap ctx, ByteBuf in, List<Object> out) throws Exception {

        in.resetReaderIndex();
        //Common decoding part
        ConnectMessage message = new ConnectMessage();
        if (!decodeCommonHeader(message, 0x00, in)) {
            in.resetReaderIndex();
            throw new CorruptedFrameException("Received a CONNECT but message not available");
        }
        int remainingLength = message.getRemainingLength();
        int start = in.readerIndex();

        int protocolNameLen = in.readUnsignedShort();
        byte[] encProtoName;
        String protoName;
        Attribute<Integer> versionAttr = ctx.attr(MQTTDecoder.PROTOCOL_VERSION);
        switch (protocolNameLen) {
            case 6:
                //MQTT version 3.1 "MQIsdp"
                //ProtocolName 8 bytes or 6 bytes
                if (in.readableBytes() < 10) {
                    in.resetReaderIndex();
                    throw new CorruptedFrameException("Received a CONNECT but message not available");
                }

                encProtoName = new byte[6];
                in.readBytes(encProtoName);
                protoName = new String(encProtoName, "UTF-8");
                if (!"MQIsdp".equals(protoName)) {
                    in.resetReaderIndex();
                    throw new CorruptedFrameException("Invalid protoName: " + protoName);
                }
                message.setProtocolName(protoName);

                versionAttr.set((int) DecoderUtils.VERSION_3_1);
                break;
            case 4:
                //MQTT version 3.1.1 "MQTT"
                //ProtocolName 6 bytes
                if (in.readableBytes() < 8) {
                    in.resetReaderIndex();
                    throw new CorruptedFrameException("Received a CONNECT but message not available");
                }

                encProtoName = new byte[4];
                in.readBytes(encProtoName);
                protoName = new String(encProtoName, "UTF-8");
                if (!"MQTT".equals(protoName)) {
                    in.resetReaderIndex();
                    throw new CorruptedFrameException("Invalid protoName: " + protoName);
                }
                message.setProtocolName(protoName);
                versionAttr.set((int) DecoderUtils.VERSION_3_1_1);
                break;
            default:
                //protocol broken
                throw new CorruptedFrameException("Invalid protoName size: " + protocolNameLen);
        }

        //ProtocolVersion 1 byte (value 0x03 for 3.1, 0x04 for 3.1.1)
        message.setProtocolVersion(in.readByte());


        //check if this is another connect from the same client on the same session
        Attribute<Boolean> connectAttr = ctx.attr(ConnectDecoder.CONNECT_STATUS);
        Boolean alreadyConnected = connectAttr.get();
        if (alreadyConnected == null) {
            //never set
            connectAttr.set(true);
        } else if (alreadyConnected) {
            throw new CorruptedFrameException("Received a second CONNECT on the same network connection");
        }


        //Connection flag
        byte connFlags = in.readByte();
        if (message.getProtocolVersion() == DecoderUtils.VERSION_3_1_1) {//3.1.1第一位必须是0
            if ((connFlags & 0x01) != 0) { //bit(0) of connection flags is != 0
                throw new CorruptedFrameException("Received a CONNECT with connectionFlags[0(bit)] != 0");
            }
        }

        boolean cleanSession = ((connFlags & 0x02) >> 1) == 1 ? true : false;
        boolean willFlag = ((connFlags & 0x04) >> 2) == 1 ? true : false;
        byte willQos = (byte) ((connFlags & 0x18) >> 3);
        if (willQos > 2) {
            in.resetReaderIndex();
            throw new CorruptedFrameException("Expected will QoS in range 0..2 but found: " + willQos);
        }
        boolean willRetain = ((connFlags & 0x20) >> 5) == 1 ? true : false;
        boolean passwordFlag = ((connFlags & 0x40) >> 6) == 1 ? true : false;
        boolean userFlag = ((connFlags & 0x80) >> 7) == 1 ? true : false;
        //a password is true iff user is true.
        if (!userFlag && passwordFlag) {
            in.resetReaderIndex();
            throw new CorruptedFrameException("Expected password flag to true if the user flag is true but was: " + passwordFlag);
        }
        message.setCleanSession(cleanSession);
        message.setWillFlag(willFlag);
        message.setWillQos(willQos);
        message.setWillRetain(willRetain);
        message.setPasswordFlag(passwordFlag);
        message.setUserFlag(userFlag);

        //Keep Alive timer 2 bytes
        //int keepAlive = Utils.readWord(in);
        int keepAlive = in.readUnsignedShort();
        message.setKeepAlive(keepAlive);

        //后面没有附加信息了，应该有用户名和密码，所以不能直接返回
//        if ((remainingLength == 12 && message.getProtocolVersion() == Utils.VERSION_3_1) ||
//                (remainingLength == 10 && message.getProtocolVersion() == Utils.VERSION_3_1_1)) {
//            out.add(message);
//            return;
//        }

        /**
         * 下面是payload
         */

        //Decode the ClientID
        String clientID = DecoderUtils.decodeString(in);
        if (clientID == null) {
            in.resetReaderIndex();
            throw new CorruptedFrameException("Received a CONNECT but client id not available");
        }
        message.setClientID(clientID);

        //Decode willTopic
        if (willFlag) {
            String willTopic = DecoderUtils.decodeString(in);
            if (willTopic == null) {
                in.resetReaderIndex();
                throw new CorruptedFrameException("Received a CONNECT but will topic not available");
            }
            message.setWillTopic(willTopic);
        }

        //Decode willMessage
        if (willFlag) {
            String willMessage = DecoderUtils.decodeString(in);
            if (willMessage == null) {
                in.resetReaderIndex();
                throw new CorruptedFrameException("Received a CONNECT but will message not available");
            }
            message.setWillMessage(willMessage);
        }

        //Compatibility check with v3.0, remaining length has precedence over
        //the user and password flags
        int readed = in.readerIndex() - start;
        if (readed == remainingLength) {
            out.add(message);
            throw new CorruptedFrameException("Received a CONNECT but user name and password should not empty");
        }

        //Decode username
        if (userFlag) {
            String userName = DecoderUtils.decodeString(in);
            if (userName == null) {
                in.resetReaderIndex();
                throw new CorruptedFrameException("Received a CONNECT but user name should not empty");
            }
            message.setUsername(userName);
        }

        readed = in.readerIndex() - start;
        if (readed == remainingLength) {
            out.add(message);
            throw new CorruptedFrameException("Received a CONNECT but user password should not empty");
        }

        //Decode password
        if (passwordFlag) {
            String password = DecoderUtils.decodeString(in);
            if (password == null) {
                in.resetReaderIndex();
                throw new CorruptedFrameException("Received a CONNECT but password should not empty");
            }
            message.setPassword(password);
        }

        out.add(message);

    }
}
