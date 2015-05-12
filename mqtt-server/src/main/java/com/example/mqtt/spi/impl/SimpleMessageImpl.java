package com.example.mqtt.spi.impl;

import com.example.mqtt.event.listener.LoginEvent;
import com.example.mqtt.event.listener.PublishEvent;
import com.example.mqtt.proto.messages.*;
import com.example.mqtt.parser.decoder.DecoderUtils;
import com.example.mqtt.rpc.MqttListener;
import com.example.mqtt.server.ConnectionDescriptor;
import com.example.mqtt.server.Constants;
import com.example.mqtt.server.ServerChannel;
import com.example.mqtt.spi.IMessaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by guanxinquan on 15-5-6.
 */
public class SimpleMessageImpl implements IMessaging {
    private static final Logger logger = LoggerFactory.getLogger(SimpleMessageImpl.class);


    private MqttListener listener = new MqttListener();

    /**
     * client id map to connect
     */
    private Map<String,ConnectionDescriptor> clientIDs = new ConcurrentHashMap<String, ConnectionDescriptor>();

    /**
     * 临时存储被踢掉的链接，以防止重复删除
     */
    private Map<String,ConnectionDescriptor> clientIDsTemplate = new ConcurrentHashMap<String, ConnectionDescriptor>();

    /**
     * 用户名（id）与链接的映射
     */
    private Map<String,Set<ConnectionDescriptor>> names = new ConcurrentHashMap<String, Set<ConnectionDescriptor>>();

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
            clientIDsTemplate.remove(clientID);
            descriptor.setClose(true);
            logger.info("process connect lost for kick out clientId {}",clientID);
        }else{
            descriptor = clientIDs.get(clientID);
            if(descriptor != null){
                clientIDs.remove(clientID);
                logger.info("process connect lost clientId {}",clientID);
                descriptor.setClose(true);
            }
        }
        if(descriptor != null){
            String userName = (String) descriptor.getSession().getAttribute(Constants.USER_NAME);
            processClientLost(userName);
        }

    }

    void processConnect(ServerChannel session, ConnectMessage msg){

        /**
         * 版本号不对，直接拒绝链接
         */
        if(msg.getProtocolVersion() != DecoderUtils.VERSION_3_1 && msg.getProtocolVersion() != DecoderUtils.VERSION_3_1_1){
            ConnAckMessage badProto = new ConnAckMessage();
            badProto.setReturnCode(ConnAckMessage.UNNACEPTABLE_PROTOCOL_VERSION);
            logger.warn("processConnect sent bad proto ConnAck");
            session.write(badProto);
            session.close(false);
            return;
        }

        /**
         * clientId 为空拒绝访问
         */
        if(msg.getClientID() == null || msg.getClientID().length() == 0){
            ConnAckMessage okResp = new ConnAckMessage();
            okResp.setReturnCode(ConnAckMessage.IDENTIFIER_REJECTED);
            session.write(okResp);
            session.close(false);
            return;
        }

        /**
         * 重复登录，删除先前登录的链接
         */
        if(clientIDs.containsKey(msg.getClientID())){
            clientIDsTemplate.put(msg.getClientID(),clientIDs.get(msg.getClientID()));
            clientIDs.get(msg.getClientID()).getSession().close(false);
            clientIDs.get(msg.getClientID()).setClose(true);
            logger.info("Found an existing connection with same client ID <{}>, forced to close", msg.getClientID());
        }

        ConnectionDescriptor connDes = new ConnectionDescriptor(msg.getClientID(),session);
        clientIDs.put(msg.getClientID(),connDes);
        session.setAttribute(Constants.ATTR_CLIENTID,msg.getClientID());
        session.setAttribute(Constants.MESSAGE_ID,new AtomicLong());
        session.setAttribute(Constants.USER_NAME,msg.getUsername());

        /**
         * 验证不成功 拒绝访问
         */
        try {
            if (!checkAccount(msg.getUsername(), msg.getPassword(), msg.getClientID())) {
                ConnAckMessage okResp = new ConnAckMessage();
                okResp.setReturnCode(ConnAckMessage.BAD_USERNAME_OR_PASSWORD);
                session.write(okResp);
                session.close(false);
            }
        }catch (Exception e){
            ConnAckMessage okResp = new ConnAckMessage();
            okResp.setReturnCode(ConnAckMessage.SERVER_UNAVAILABLE);
            session.write(okResp);
            session.close(false);
        }

        ConnAckMessage okResp = new ConnAckMessage();
        session.write(okResp);


        /**
         * 维护names的映射关系
         */
        Set<ConnectionDescriptor> cds = names.get(msg.getUsername());
        if(cds == null){
            cds = new CopyOnWriteArraySet<ConnectionDescriptor>();
            names.put(msg.getUsername(),cds);
        }
        cds.add(connDes);
        logger.info("Create persistent session for clientID {}", msg.getClientID());
    }

    void processPublish(ServerChannel session, PublishMessage msg){
        String clientID = (String) session.getAttribute(Constants.ATTR_CLIENTID);
        String userName = (String) session.getAttribute(Constants.USER_NAME);
        try {
            listener.eventArrival(new PublishEvent(clientID,Long.valueOf(userName),msg.getPayload().array(),msg.getTopicName()));
        } catch (RemoteException e) {//服务端发生异常，断开客户端链接
            session.close(true);
            return;
        }
        PubAckMessage pubAckMessage = new PubAckMessage();
        pubAckMessage.setMessageID(msg.getMessageID());
        session.write(pubAckMessage);
    }

    /**
     * 链接断开时，需要删除names下面对应的链接
     * @param userName 检验指定userName下相关的链接，并将所有无效的链接删除
     */
    private void processClientLost(String userName){
        Set<ConnectionDescriptor> connectionDescriptors = names.get(userName);

        List<ConnectionDescriptor> lostConnect = new ArrayList<ConnectionDescriptor>();
        if(connectionDescriptors != null) {
            for (ConnectionDescriptor cd : connectionDescriptors) {
                if (cd.isClose()) {
                    lostConnect.add(cd);
                }
            }

            for (ConnectionDescriptor cd : lostConnect) {
                logger.debug("remove name connect from names {} ", userName);
                connectionDescriptors.remove(cd);
            }
            if (connectionDescriptors.isEmpty()){
                names.remove(userName);
            }
        }

    }

    private Boolean checkAccount(String userName,String password,String clientId) throws RemoteException {
        return (Boolean) listener.eventArrival(new LoginEvent(Long.valueOf(userName),clientId,password));
    }

    public void sendMessage(String clientId,byte[] content,String topic){
        ConnectionDescriptor descriptor = clientIDs.get(clientId);
        PublishMessage publishMessage = new PublishMessage();
        publishMessage.setPayload(ByteBuffer.wrap(content));

        AtomicLong number = (AtomicLong) descriptor.getSession().getAttribute(Constants.MESSAGE_ID);
        int messageID = (int)number.incrementAndGet()%4096;
        publishMessage.setMessageID(messageID);
        publishMessage.setTopicName(topic);
        publishMessage.setQos(AbstractMessage.QOSType.LEAST_ONE);

        descriptor.getSession().write(publishMessage);
    }

    @Override
    public void sendMessageByUser(String userId, byte[] content, String topic) {

        Set<ConnectionDescriptor> descriptors = names.get(userId);
        boolean isCheckout = false;
        if(descriptors != null){
            for(ConnectionDescriptor d : descriptors){
                if(d.isClose())
                    isCheckout = true;
                else
                    sendMessage(d.getClientID(),content,topic);
            }
            if(isCheckout){
                processClientLost(userId);
            }
        }


    }

    @Override
    public int getConnectCnt() {
        return clientIDs.size();
    }

    @Override
    public int getConnectCntByUserId(String userId) {
        if(names.get(userId)!=null){
            return names.get(userId).size();
        }
        return 0;
    }
}
