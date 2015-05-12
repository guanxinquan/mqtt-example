package com.example.mqtt.server.netty;

import com.example.mqtt.parser.decoder.DecoderUtils;
import static com.example.mqtt.proto.messages.AbstractMessage.*;

import com.example.mqtt.proto.messages.AbstractMessage;
import com.example.mqtt.proto.messages.PingRespMessage;
import com.example.mqtt.server.Constants;
import com.example.mqtt.spi.IMessageFactory;
import com.example.mqtt.spi.IMessaging;
import com.example.mqtt.spi.impl.SimpleMessageImpl;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by guanxinquan on 15-5-5.
 */
@ChannelHandler.Sharable
public class NettyMQTTHandler extends ChannelHandlerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(NettyMQTTHandler.class);
    private final Map<ChannelHandlerContext, NettyChannel> channelMapper = new ConcurrentHashMap<ChannelHandlerContext, NettyChannel>();

    private IMessaging messaging = IMessageFactory.getInstance();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
        AbstractMessage msg = (AbstractMessage) message;
        LOG.info("Received a message of type {}", DecoderUtils.msgType2String(msg.getMessageType()));
        try {
            switch (msg.getMessageType()) {
                case CONNECT:
                case SUBSCRIBE:
                case UNSUBSCRIBE:
                case PUBLISH:
                case PUBREC:
                case PUBCOMP:
                case PUBREL:
                case DISCONNECT:
                case PUBACK:
                    if(!channelMapper.containsKey(ctx)){
                        channelMapper.put(ctx,new NettyChannel(ctx));
                    }
                    NettyChannel channel = channelMapper.get(ctx);

                    messaging.handleProtocolMessage(channel,msg);

                    break;
                case PINGREQ:
                    channel = channelMapper.get(ctx);
                    String clientId = (String) channel.getAttribute(Constants.ATTR_CLIENTID);
                    PingRespMessage pingResp = new PingRespMessage();
                    ctx.writeAndFlush(pingResp);
                    break;
            }
        } catch (Exception ex) {
            LOG.error("Bad error in processing the message", ex);
        }


    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NettyChannel channel = channelMapper.get(ctx);
        String clientID = (String) channel.getAttribute(Constants.ATTR_CLIENTID);
        ctx.close();
        messaging.lostConnection(clientID);
        channelMapper.remove(ctx);

    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            IdleStateEvent e = (IdleStateEvent) evt;
            if(e.state() == IdleState.READER_IDLE){
                ctx.close();
            }

        }
    }

}



