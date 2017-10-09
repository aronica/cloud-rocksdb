package redis;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.redis.RedisBulkStringAggregator;
import io.netty.handler.codec.redis.RedisDecoder;
import io.netty.handler.codec.redis.RedisEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Created by fafu on 2017/7/10.
 */
public class MasterRedisServer  {
    private static final Logger log = LoggerFactory.getLogger(MasterRedisServer.class);
    private EventLoopGroup eventLoopGroup;
    private ServerBootstrap bootstrap;
    ChannelFuture future;
    private EtcdClientProxy proxy ;


    public MasterRedisServer(String kvAddress) {
        this.proxy = new EtcdClientProxy(kvAddress);
    }


    public void doInit() throws Exception {
        this.eventLoopGroup = new NioEventLoopGroup(4);//设置并发度
        this.bootstrap = new ServerBootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress("127.0.0.1", 10000))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new RedisEncoder())
                                .addLast(new RedisDecoder())
                                .addLast(new RedisBulkStringAggregator())
                                .addLast(new RedisBulkMessageAggregator())
                                .addLast(new RedisBulkMessageScatter())
                                .addLast(new MasterRedisHandler(MasterRedisServer.this.proxy));
                    }
                });
    }
    public void doShutdown() throws Exception {
        eventLoopGroup.shutdownGracefully(10000,10000, TimeUnit.MICROSECONDS);
    }

    public void doStartup() throws Exception {
        this.future = bootstrap.bind().sync();
    }

    public static void main(String[] args) throws Exception {
        MasterRedisServer masterServer = new MasterRedisServer("http://localhost:2379,http://localhost:22379,http://localhost:32379");
        masterServer.doInit();
        masterServer.doStartup();
    }
}
