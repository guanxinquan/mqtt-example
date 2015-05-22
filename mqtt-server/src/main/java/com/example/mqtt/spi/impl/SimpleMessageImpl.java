package com.example.mqtt.spi.impl;

import com.example.mqtt.cluster.LookupService;
import com.example.mqtt.cluster.LookupServiceFactory;
import com.example.mqtt.config.MqttConfig;
import com.example.mqtt.event.listener.LoginEvent;
import com.example.mqtt.event.listener.PublishEvent;
import com.example.mqtt.logger.StatLogger;
import com.example.mqtt.mq.MqMessageOperator;
import com.example.mqtt.proto.messages.*;
import com.example.mqtt.parser.decoder.DecoderUtils;
import com.example.mqtt.rpc.MqttListener;
import com.example.mqtt.server.ConnectionDescriptor;
import com.example.mqtt.server.Constants;
import com.example.mqtt.server.ServerChannel;
import com.example.mqtt.server.netty.NettyAcceptor;
import com.example.mqtt.spi.IMessaging;
import com.example.mqtt.store.PubStubStore;
import com.example.mqtt.store.QosFlightStore;
import com.example.mqtt.store.QosPubStoreEvent;
import com.example.mqtt.store.impl.GuavaPubStubStore;
import com.example.mqtt.store.impl.QosFlightStoreImpl;
import com.example.mqtt.zk.IZkServer;
import com.example.mqtt.zk.ZkServerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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

    private LookupService lookupService = LookupServiceFactory.getInstance();

    private PubStubStore stubStore = new GuavaPubStubStore();

    private QosFlightStore flightStore = new QosFlightStoreImpl();

    private IZkServer zkServer = ZkServerFactory.getInstance();

    private MqMessageOperator mqMessageOperator = new MqMessageOperator();

    private static final String EXCHANGE_NAME = "mqtt-pub";

    private static final String ROUT_KEY = "mqtt-queue";

    private static final ObjectMapper mapper = new ObjectMapper();


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

            processPubAck(session,pubAckMessage);
            logger.info("pubAck message,message id is {}", pubAckMessage.getMessageID());
        }else if(msg instanceof DisconnectMessage){
            DisconnectMessage disconnectMessage = (DisconnectMessage) msg;
            logger.info("disconnect message");
            session.close(true);
        }else if(msg instanceof PingReqMessage){
            processPingReq(session,msg);
        }
    }

    private void processPingReq(ServerChannel session, AbstractMessage msg) {
        String userName = (String) session.getAttribute(Constants.USER_NAME);
        try {
            String service = lookupService.lookUpServer(userName);

            if(!service.equals(MqttConfig.getMqttUrl())){
                session.close(true);
                return;
            }
        } catch (Exception e) {
            logger.error("mqtt ping process error",e);
            return;
        }


        logger.debug("ping response userName {}",userName);
        PingRespMessage pingResp = new PingRespMessage();
        session.write(pingResp);
    }

    private void processPubAck(ServerChannel session, PubAckMessage pubAckMessage) {
        String clientID = (String) session.getAttribute(Constants.ATTR_CLIENTID);
        flightStore.removeFlight(clientID,String.valueOf(pubAckMessage.getMessageID()));
    }

    @Override
    public void lostConnection(String clientID) {
        if(clientID != null) {
            ConnectionDescriptor descriptor = clientIDsTemplate.get(clientID);
            if (descriptor != null) {
                clientIDsTemplate.remove(clientID);
                descriptor.setClose(true);
                logger.info("process connect lost for kick out clientId {}", clientID);
            } else {
                descriptor = clientIDs.get(clientID);
                if (descriptor != null) {
                    clientIDs.remove(clientID);
                    logger.info("process connect lost clientId {}", clientID);
                    descriptor.setClose(true);
                }
            }
            if (descriptor != null) {
                String userName = (String) descriptor.getSession().getAttribute(Constants.USER_NAME);
                processClientLost(userName);
            }
        }

    }

    void processConnect(ServerChannel session, ConnectMessage msg){


        /**
         * 先验证用户是否连接到正确的server。
         */
        String userName = msg.getUsername();
        if(!checkHosts(userName)){
            ConnAckMessage badProto = new ConnAckMessage();
            badProto.setReturnCode(ConnAckMessage.NOT_AUTHORIZED);
            logger.error("client access a error mqtt host {}",userName,new Exception());
            session.write(badProto);
            session.close(false);
            return;
        }

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
         * 验证不成功 拒绝访问
         */
        try {
            if (!checkAccount(msg.getUsername(), msg.getPassword(), msg.getClientID())) {
                ConnAckMessage okResp = new ConnAckMessage();
                okResp.setReturnCode(ConnAckMessage.BAD_USERNAME_OR_PASSWORD);
                session.write(okResp);
                session.close(false);
                return;
            }
        }catch (Exception e){
            ConnAckMessage okResp = new ConnAckMessage();
            okResp.setReturnCode(ConnAckMessage.SERVER_UNAVAILABLE);
            session.write(okResp);
            session.close(false);
        }

        /**
         * 重复登录，删除先前登录的链接
         */
        if(clientIDs.containsKey(msg.getClientID())){

            ConnectionDescriptor older = clientIDs.get(msg.getClientID());
            clientIDsTemplate.put(msg.getClientID(),older);
            older.getSession().close(false);
            older.setClose(true);
            logger.info("Found an existing connection with same client ID <{}>, forced to close", msg.getClientID());
        }

        ConnectionDescriptor connDes = new ConnectionDescriptor(msg.getClientID(),session);
        clientIDs.put(msg.getClientID(),connDes);
        session.setAttribute(Constants.ATTR_CLIENTID,msg.getClientID());
        session.setAttribute(Constants.MESSAGE_ID,new AtomicLong());
        session.setAttribute(Constants.USER_NAME,msg.getUsername());



        ConnAckMessage okResp = new ConnAckMessage();
        session.write(okResp);

        StatLogger.logger(StatLogger.CONNECT,msg.getUsername(),msg.getClientID(),null);
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

        StatLogger.logger(StatLogger.PUB_UP,userName,clientID,String.valueOf(msg.isDupFlag()));

        if(msg.isDupFlag()){//消息很可能是重复消息，直接过滤掉
            String stub = stubStore.fetchStub(clientID,String.valueOf(msg.getMessageID()));
            if(stub != null){//可以判断是重复消息，直接给出ack
                PubAckMessage pubAckMessage = new PubAckMessage();
                pubAckMessage.setMessageID(msg.getMessageID());
                session.write(pubAckMessage);
                return;
            }
        }

        try {//使用rabbitmq发送消息
            PublishEvent event = new PublishEvent(clientID,Long.valueOf(userName),msg.getPayload().array(),msg.getTopicName());
            mqMessageOperator.publish(EXCHANGE_NAME,ROUT_KEY,null,mapper.writeValueAsBytes(event));
            //listener.eventArrival(new PublishEvent(clientID,Long.valueOf(userName),msg.getPayload().array(),msg.getTopicName()));
        } catch (Exception e) {//服务端发生异常，断开客户端链接
            logger.error("send message to rabbit mq error",e);
            session.close(true);
            return;
        }
        stubStore.storeStub(clientID,String.valueOf(msg.getMessageID()));
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

        logger.info("clientId is {},content is {} ,topic is {}, descriptor {}",clientId,new String(content),topic,descriptor);
        AtomicLong number = (AtomicLong) descriptor.getSession().getAttribute(Constants.MESSAGE_ID);
        String userName = (String) descriptor.getSession().getAttribute(Constants.USER_NAME);
        int messageID = (int)number.incrementAndGet()%4096;
        publishMessage.setMessageID(messageID);
        publishMessage.setTopicName(topic);
        publishMessage.setQos(AbstractMessage.QOSType.LEAST_ONE);

        QosPubStoreEvent event = new QosPubStoreEvent(clientId,publishMessage);
        flightStore.storeFlight(clientId,String.valueOf(messageID),event);
        descriptor.getSession().write(publishMessage);

        StatLogger.logger(StatLogger.PUB_DOWN,userName,clientId,null);

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

    @Override
    public long getPubStubCnt() {
        return stubStore.getSize();
    }

    @Override
    public long getQosFlightCnt() {
        return flightStore.getSize();
    }

    @Override
    public void close() throws IOException {
        flightStore.close();
        stubStore.close();
    }

    /**
     * 验证当前登录的用户，是否可以连接到本机上
     * @param userName
     * @return
     */
    private boolean checkHosts(String userName){
        String server = zkServer.fetchServer(userName);
        if(NettyAcceptor.LOCAL_SERVER_IDENTIFY.equals(server)){
            return true;
        }
        return false;
    }

    @Override
    public boolean republish(QosPubStoreEvent event) {
        ConnectionDescriptor descriptor = clientIDs.get(event.getClientID());
        if(descriptor == null || descriptor.isClose()){//如果客户端已经断开连接，直接返回false
            logger.debug("repub message clientId {} error , connection already closed",event.getClientID());
            return false;
        }
        /**
         * 重新发送消息
         */
        logger.info("repub message clientId {} ",event.getClientID());
        event.getMessage().setDupFlag(true);
        event.getMessage().reset();
        descriptor.getSession().write(event.getMessage());
        String userName = (String) descriptor.getSession().getAttribute(Constants.USER_NAME);
        StatLogger.logger(StatLogger.PUB_RETRY,userName,event.getClientID(),String.valueOf(event.getCnt()));
        return true;
    }

    @Override
    public boolean kickOut(String clientID) {
        ConnectionDescriptor descriptor = clientIDs.get(clientID);
        if(descriptor != null && !descriptor.isClose()) {
            descriptor.getSession().close(true);
            descriptor.setClose(true);

            return true;
        }else{
            return false;
        }
    }
}
