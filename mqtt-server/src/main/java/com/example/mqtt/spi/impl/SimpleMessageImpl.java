package com.example.mqtt.spi.impl;

import com.example.mqtt.proto.messages.*;
import com.example.mqtt.parser.decoder.DecoderUtils;
import com.example.mqtt.server.ConnectionDescriptor;
import com.example.mqtt.server.Constants;
import com.example.mqtt.server.ServerChannel;
import com.example.mqtt.spi.IMessaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by guanxinquan on 15-5-6.
 */
public class SimpleMessageImpl implements IMessaging {
    private static final Logger logger = LoggerFactory.getLogger(SimpleMessageImpl.class);


    /**
     * client id map to connect
     */
    private Map<String,ConnectionDescriptor> clientIDs = new ConcurrentHashMap<String, ConnectionDescriptor>();

    private Map<String,ConnectionDescriptor> clientIDsTemplate = new ConcurrentHashMap<String, ConnectionDescriptor>();

    @Override
    public void handleProtocolMessage(ServerChannel session, AbstractMessage msg) {
        if(msg instanceof ConnectMessage){
            ConnectMessage connectMessage = (ConnectMessage) msg;
            logger.info("connect message ,connect user name is {} password is {}",connectMessage.getUsername(),connectMessage.getPassword());
            processConnect(session, (ConnectMessage) msg);
        }else if(msg instanceof PublishMessage){
            PublishMessage publishMessage = (PublishMessage) msg;
            logger.info("publish message , topic is {} content is {}",publishMessage.getTopicName(),new String(publishMessage.getPayload().array()));
            processPublish(session, (PublishMessage) msg);
        }else if(msg instanceof PubAckMessage){
            PubAckMessage pubAckMessage = (PubAckMessage) msg;
            logger.info("pubAck message,message id is {}", pubAckMessage.getMessageID());
        }else if(msg instanceof DisconnectMessage){
            DisconnectMessage disconnectMessage = (DisconnectMessage) msg;
            logger.info("disconnect message");
            session.close(true);
        }
    }

    @Override
    public void lostConnection(String clientID) {
        ConnectionDescriptor descriptor = clientIDsTemplate.get(clientID);
        if(descriptor != null){
            clientIDs.remove(clientID);
            logger.info("process connect lost for kick out clientId {}",clientID);
        }else{
            descriptor = clientIDs.get(clientID);
            if(descriptor != null){
                clientIDs.remove(clientID);
                logger.info("process connect lost clientId {}",clientID);
            }
        }
        if(descriptor != null){
            processClientLost(clientID);
        }

    }

    void processConnect(ServerChannel session, ConnectMessage msg){

        if(msg.getProtocolVersion() != DecoderUtils.VERSION_3_1 && msg.getProtocolVersion() != DecoderUtils.VERSION_3_1_1){
            ConnAckMessage badProto = new ConnAckMessage();
            badProto.setReturnCode(ConnAckMessage.UNNACEPTABLE_PROTOCOL_VERSION);
            logger.warn("processConnect sent bad proto ConnAck");
            session.write(badProto);
            session.close(false);
            return;
        }

        if(msg.getClientID() == null || msg.getClientID().length() == 0){
            ConnAckMessage okResp = new ConnAckMessage();
            okResp.setReturnCode(ConnAckMessage.IDENTIFIER_REJECTED);
            session.write(okResp);
            session.close(false);
            return;
        }

        if(clientIDs.containsKey(msg.getClientID())){
            clientIDsTemplate.put(msg.getClientID(),clientIDs.get(msg.getClientID()));
            clientIDs.get(msg.getClientID()).getSession().close(false);
            logger.info("Found an existing connection with same client ID <{}>, forced to close", msg.getClientID());
        }

        ConnectionDescriptor connDes = new ConnectionDescriptor(msg.getClientID(),session);
        clientIDs.put(msg.getClientID(),connDes);
        session.setAttribute(Constants.ATTR_CLIENTID,msg.getClientID());
        session.setAttribute(Constants.MESSAGE_ID,new AtomicLong());
        session.setAttribute(Constants.USER_NAME,msg.getUsername());

        if(!checkAccount(msg.getUsername(),msg.getPassword())){
            ConnAckMessage okResp = new ConnAckMessage();
            okResp.setReturnCode(ConnAckMessage.BAD_USERNAME_OR_PASSWORD);
            session.write(okResp);
            session.close(false);
        }

        ConnAckMessage okResp = new ConnAckMessage();
        session.write(okResp);
        logger.info("Create persistent session for clientID {}", msg.getClientID());
    }

    void processPublish(ServerChannel session, PublishMessage msg){
        String clientID = (String) session.getAttribute(Constants.ATTR_CLIENTID);
        String userName = (String) session.getAttribute(Constants.USER_NAME);
        messageArrive(clientID,new String(msg.getPayload().array()));
        PubAckMessage pubAckMessage = new PubAckMessage();
        pubAckMessage.setMessageID(msg.getMessageID());
        session.write(pubAckMessage);
    }

    void processClientLost(String clientID){

    }


    private boolean checkAccount(String userName,String password){
        if(userName.equals(password)){
            return true;
        }
        return false;
    }

    private void messageArrive(String clientId,String content){
        sendMessage(clientId,content,"echo");


    }

    private void sendMessage(String clientId,String content,String topic){
        ConnectionDescriptor descriptor = clientIDs.get(clientId);
        PublishMessage publishMessage = new PublishMessage();
        publishMessage.setPayload(ByteBuffer.wrap(content.getBytes()));

        AtomicLong number = (AtomicLong) descriptor.getSession().getAttribute(Constants.MESSAGE_ID);
        int messageID = (int)number.incrementAndGet()%4096;
        publishMessage.setMessageID(messageID);
        publishMessage.setTopicName(topic);
        publishMessage.setQos(AbstractMessage.QOSType.LEAST_ONE);

        descriptor.getSession().write(publishMessage);
    }
}
