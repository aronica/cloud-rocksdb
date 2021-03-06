package cloud.rocksdb.server.master;

import cloud.rocksdb.server.AbstractServer;
import cloud.rocksdb.server.ReplicatorDecoder;
import cloud.rocksdb.server.ReplicatorEncoder;
import cloud.rocksdb.server.config.Configuration;
import cloud.rocksdb.server.util.ShutdownHookManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Created by fafu on 2017/7/10.
 */
public class MasterServer extends AbstractServer {
    private static final Logger log = LoggerFactory.getLogger(MasterServer.class);
    private ShardDiscover shardDiscover;
    private EventLoopGroup eventLoopGroup;
    private ServerBootstrap bootstrap;
    ChannelFuture future;

    @Override
    protected int doGetPort() {
        return config.getMasterPort();
    }

    public MasterServer(Configuration config){
        super(config);
    }

    @Override
    public void doInit() throws Exception {
        shardDiscover = new ShardDiscover(config,curator);
        this.eventLoopGroup = new NioEventLoopGroup(config.getInstanceConcurrency());//设置并发度
        this.bootstrap = new ServerBootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(this.host, this.port))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(config.getRequestMaxBodyLength(), 0, 4));
                        ch.pipeline().addLast(new ReplicatorDecoder());
                        ch.pipeline().addLast(new ReplicatorEncoder());
                        ch.pipeline().addLast(new MasterProxyHandler(shardDiscover));
                    }
                });
        shardDiscover.init();
    }

    @Override
    public void doShutdown() throws Exception {
        eventLoopGroup.shutdownGracefully(10000,10000, TimeUnit.MICROSECONDS);
        shardDiscover.shutdown();
    }

    @Override
    public void doStartup() throws Exception {
        shardDiscover.startup();
        this.future = bootstrap.bind().sync();
    }



    public static void main(String[] args) throws Exception {
        Configuration config = new Configuration();
        MasterServer masterServer = new MasterServer(config);
        masterServer.init();
        masterServer.startup();
        ShutdownHookManager.get().addShutdownHook(()->{
            try {
                masterServer.shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        },3);
    }
}
