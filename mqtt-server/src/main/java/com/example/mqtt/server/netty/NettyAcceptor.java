package com.example.mqtt.server.netty;

import com.example.mqtt.spi.IMessaging;
import com.example.mqtt.parser.decoder.MQTTDecoder;
import com.example.mqtt.parser.encoder.MQTTEncoder;
import com.example.mqtt.server.ServerAcceptor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by guanxinquan on 15-5-5.
 */
public class NettyAcceptor implements ServerAcceptor {

    private static final Logger logger = LoggerFactory.getLogger(NettyAcceptor.class);

    private static final int DEFAULT_CONNECT_TIMEOUT = 10;

    private static final int DEFAULT_PORT = 1883;

    EventLoopGroup bossGroup;

    EventLoopGroup workerGroup;

    @Override
    public void initialize(IMessaging messaging, Properties props) throws IOException {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        initializePlainTCPTransport(messaging,props);
    }

    private void initFactory(String host, int port) {
        ServerBootstrap b = new ServerBootstrap();
        final NettyMQTTHandler handler = new NettyMQTTHandler();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addFirst("idleStateHandler", new IdleStateHandler(0, 0, DEFAULT_CONNECT_TIMEOUT));
                        pipeline.addLast("decoder", new MQTTDecoder());
                        pipeline.addLast("encoder", new MQTTEncoder());
                        pipeline.addLast("handler", handler);
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        try {
            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(host, port);
            logger.info("Server binded host: {}, port: {}", host, port);
            f.sync();
        } catch (InterruptedException ex) {
            logger.error(null, ex);
        }
    }

    private void initializePlainTCPTransport(IMessaging messaging, Properties props) throws IOException {
        String host = props.getProperty("host");
        System.err.println(props.getProperty("port"));
        //Integer port = Integer.valueOf(props.getProperty("port"));
        //if(port == null)
           // port = DEFAULT_PORT;
        if(host == null){
            host = "localhost";
        }
        initFactory(host, DEFAULT_PORT);
    }

    @Override
    public void close() {
        if(workerGroup != null){
            workerGroup.shutdownGracefully();
        }
        if(bossGroup != null){
            bossGroup.shutdownGracefully();
        }


    }
}
