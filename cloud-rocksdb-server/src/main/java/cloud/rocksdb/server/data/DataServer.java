package cloud.rocksdb.server.data;

import cloud.rocksdb.server.AbstractServer;
import cloud.rocksdb.server.ReplicatorDecoder;
import cloud.rocksdb.server.ReplicatorEncoder;
import cloud.rocksdb.server.config.Configuration;
import cloud.rocksdb.server.db.RocksDBHolder;
import cloud.rocksdb.server.master.Container;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.UriSpec;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Created by fafu on 2017/5/30.
 */
public class DataServer extends AbstractServer {
    private static final Logger log = LoggerFactory.getLogger(DataServer.class);

    private RocksDBHolder rocksDBHolder;
    private EventLoopGroup eventLoopGroup;
    private ServerBootstrap bootstrap;
    private String shardId;
    private ServiceDiscovery<Container> serviceDiscovery;

    public DataServer(Configuration config, String shardId) {
        super(config);
        this.shardId = shardId;
    }

    public void initStorage() throws Exception {
        RocksDBHolder holder = new RocksDBHolder(shardId);
        holder.init();
        this.rocksDBHolder = holder;

    }

    public void startupInstance() throws Exception {
        this.eventLoopGroup = new NioEventLoopGroup(config.getInstanceConcurrency());//设置并发度
        try {
            this.bootstrap = new ServerBootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(this.host, this.port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(config.getRequestMaxBodyLength(), 0, 4));
                            ch.pipeline().addLast(new ReplicatorDecoder());
                            ch.pipeline().addLast(new ReplicatorEncoder());
                            ch.pipeline().addLast(new DataHandler(rocksDBHolder));
                        }
                    });

            ChannelFuture future = bootstrap.bind().sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("", e);
            throw e;
        }
    }

    //register this to master
    private void register() throws Exception {
        UriSpec spec = new UriSpec("{schema}://{address}:{port}");
        ServiceInstance<Container> instance = ServiceInstance.<Container>builder().name(shardId).id(host+"_"+port).payload(new Container(shardId,host,port)).address(host).port(port).uriSpec(spec).build();
        serviceDiscovery = ServiceDiscoveryBuilder
                .builder(Container.class).client(curator)
                .serializer(new JsonInstanceSerializer<Container>(Container.class))
                .basePath(config.getZkServiceDiscoveryPath()).build();
        serviceDiscovery.start();
        serviceDiscovery.registerService(instance);
    }



    @Override
    public void doInit() throws Exception {
        initStorage();
    }

    @Override
    public void doStartup() throws Exception {
        rocksDBHolder.startup();
        startupInstance();
        register();
    }


    @Override
    public void doShutdown() throws Exception {
        try {
            eventLoopGroup.shutdownGracefully(10, 10, TimeUnit.SECONDS);
            Thread.sleep(10000);
            rocksDBHolder.shutdown();//强制shutdown
            serviceDiscovery.close();
            curator.close();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        DataServer dataServer = null;
        try {
            Configuration config = new Configuration();
            dataServer = new DataServer(config, "1");
            dataServer.init();
            dataServer.startup();
            Scanner scanner = new Scanner(System.in);
            if("quit".equals(scanner.nextLine())){
                dataServer.clone();
            }
            System.exit(0);
        }catch (Exception e){
            e.printStackTrace();
            if(dataServer != null)dataServer.clone();
        }

    }
}
