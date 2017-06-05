package cloud.rocksdb.server;

import cloud.rocksdb.ReplicatorDecoder;
import cloud.rocksdb.ReplicatorEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.rocksdb.RocksDBException;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;

/**
 * Created by fafu on 2017/5/30.
 */
public class Server {

    public static void main(String[] args) throws UnsupportedEncodingException, RocksDBException {
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
