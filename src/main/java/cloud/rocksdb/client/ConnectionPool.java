package cloud.rocksdb.client;

import cloud.rocksdb.ReplicatorDecoder;
import cloud.rocksdb.ReplicatorEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolHandler;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.AttributeKey;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by fafu on 2017/5/30.
 */
@Data
public class ConnectionPool {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);
    private FixedChannelPool pool;
    private Bootstrap bootstrap;
    private EventLoopGroup group;
    public static final String CONNECTION_KEY = "connection";
    public Map<Channel,Connection> channelConnectionMap = new ConcurrentHashMap<>();
    public static final AttributeKey<TaskFuture> NETTY_CHANNEL_KEY = AttributeKey.valueOf(CONNECTION_KEY);


    public ConnectionPool(String host, int port, int poolsize,int timeout){
        EventLoopGroup group = new NioEventLoopGroup();
        try{
            Bootstrap bootstrap = new Bootstrap();
            SimpleClientHandler handler = new SimpleClientHandler();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.SO_LINGER, 0)
//                    .option(ChannelOption.SO_TIMEOUT,timeout)
                    .remoteAddress(host,port);
//            ChannelFuture future = bootstrap.connect(host, port).sync();
            this.bootstrap = bootstrap;
            this.pool = new FixedChannelPool(bootstrap,new AbstractChannelPoolHandler(){
                @Override
                public void channelCreated(Channel ch) throws Exception {
                    logger.info("Channel created!");
                    ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024*1024*1024,0,4));
                    ch.pipeline().addLast(new ReplicatorEncoder());
                    ch.pipeline().addLast(new ReplicatorDecoder());
                    ch.pipeline().addLast(handler);
//                    ch.attr(NETTY_CHANNEL_KEY);
                }
            },poolsize);
        } catch (Exception e) {
            logger.error("",e);
            throw new RuntimeException("Unable to create connection pool.");
        } finally {
//            try {
//                group.shutdownGracefully().sync();
//                bootstrap.clone();
//            } catch (InterruptedException e) {
//                logger.error("",e);
//            }
        }
    }

    public Connection getConnection() throws ExecutionException, InterruptedException {
        Future<Channel> channelFuture =  pool.acquire().syncUninterruptibly();
        Channel channel = channelFuture.get();
        Connection connection = channelConnectionMap.get(channel);
        if(connection == null){
            connection = new Connection(channel);
            Connection oldconnection = channelConnectionMap.putIfAbsent(channel,connection);
            if(oldconnection != null) connection = oldconnection;
        }
        return connection;
    }

    public void returnConnectoin(Connection connection){
        if(connection != null)
            pool.release(connection.getChannel()).syncUninterruptibly();
    }

    public void destroy() throws InterruptedException {
        group.shutdownGracefully().sync();
    }
}
