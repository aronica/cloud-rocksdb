package cloud.rocksdb.server;

import cloud.rocksdb.ReplicatorDecoder;
import cloud.rocksdb.ReplicatorEncoder;
import cloud.rocksdb.db.InstanceAlreadyRunningException;
import cloud.rocksdb.db.RocksDBHolder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;

/**
 * Created by fafu on 2017/5/30.
 */
public class Server {
    private static final Logger log = LoggerFactory.getLogger(Server.class);
    private String id;
    private RocksDBHolder rocksDBHolder;

    public Server(String id){
        this.id = id;
    }

    public void initConfig(){

    }

    public void initStorage() throws InstanceAlreadyRunningException {
        RocksDBHolder holder = new RocksDBHolder(id);
        this.rocksDBHolder = holder;
    }

    public void initServingConnect() throws UnsupportedEncodingException, RocksDBException {
        EventLoopGroup eventLoop = new NioEventLoopGroup();
        try{
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(eventLoop)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(8888))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024*1024*1024,0,4));
                            ch.pipeline().addLast(new ReplicatorDecoder());
                            ch.pipeline().addLast(new ReplicatorEncoder());
                            ch.pipeline().addLast(new ServerHandler());
                        }
                    });
            ChannelFuture future = bootstrap.bind().sync();
            future.channel().closeFuture().sync();
            System.out.println("Success starting at 8888");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            try {
                eventLoop.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
