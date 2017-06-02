package cloud.rocksdb.client;

import cloud.rocksdb.ReplicatorEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolHandler;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

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


    public ConnectionPool(String host, int port, int poolsize){
        EventLoopGroup group = new NioEventLoopGroup();
        try{
            Bootstrap bootstrap = new Bootstrap();
            SimpleClientHandler handler = new SimpleClientHandler();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .remoteAddress(host,port)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ReplicatorEncoder());
                            ch.pipeline().addLast(handler);
                        }
                    });
//            ChannelFuture future = bootstrap.connect(host, port).sync();
            this.bootstrap = bootstrap;
            this.pool = new FixedChannelPool(bootstrap,new AbstractChannelPoolHandler(){
                @Override
                public void channelCreated(Channel ch) throws Exception {
                    logger.info("Channel created!");
                }
            },poolsize);
        } catch (Exception e) {
            logger.error("",e);
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
        Channel channel =  pool.acquire().get();
        Connection connection = channelConnectionMap.get(channel);
        if(connection == null){
            connection = new Connection(channel);
            connection = channelConnectionMap.putIfAbsent(channel,connection);
        }
        return connection;
    }

    public void returnConnectoin(Connection connection){
        pool.release(connection.getChannel());
    }
}
