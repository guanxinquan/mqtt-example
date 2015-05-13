package com.example.mqtt.server.netty;

import com.example.mqtt.jmx.MqttStatus;
import com.example.mqtt.register.MqttRegister;
import com.example.mqtt.rpc.RpcServiceRegister;
import com.example.mqtt.spi.IMessaging;
import com.example.mqtt.parser.decoder.MQTTDecoder;
import com.example.mqtt.parser.encoder.MQTTEncoder;
import com.example.mqtt.server.ServerAcceptor;
import com.example.mqtt.zk.IZkServer;
import com.example.mqtt.zk.ZkServerFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
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

    private IZkServer zkServer = ZkServerFactory.getInstance();

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

            //注册jxm bean
            try {
                MBeanServer server = ManagementFactory.getPlatformMBeanServer();
                ObjectName mbeanName = new ObjectName("com.example.mqtt.jmx:type=MqttStatusMBean");
                MqttStatus mbean = new MqttStatus();
                server.registerMBean(mbean,mbeanName);
            }catch (Exception e){
                logger.error("register jmx bean error ",e);
            }

            /**
             * 注册mqtt服务
             */
            try {
                String registerInfo = host+":"+port+":"+RpcServiceRegister.port;
                zkServer.registerServer(registerInfo,null);
            } catch (Exception e) {
                logger.error("register to mqtt service error ",e);
                System.exit(1);
            }

            Runtime.getRuntime().addShutdownHook(new Shutdown(this));

            ChannelFuture f = b.bind(host, port);
            logger.info("Server binded host: {}, port: {}", host, port);
            f.sync();
        } catch (InterruptedException ex) {
            logger.error(null, ex);
            System.exit(1);
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

        logger.info("netty service shut down");

        if(workerGroup != null){
            workerGroup.shutdownGracefully();
            workerGroup = null;
        }
        if(bossGroup != null){
            bossGroup.shutdownGracefully();
            bossGroup = null;
        }


    }
}

class Shutdown extends Thread{
    private NettyAcceptor acceptor;

    Shutdown(NettyAcceptor acceptor){
        this.acceptor = acceptor;
    }

    @Override
    public void run() {
        acceptor.close();
    }
}
