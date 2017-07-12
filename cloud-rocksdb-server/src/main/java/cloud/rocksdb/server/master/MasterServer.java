package cloud.rocksdb.server.master;

import cloud.rocksdb.server.AbstractServer;
import cloud.rocksdb.server.ReplicatorDecoder;
import cloud.rocksdb.server.ReplicatorEncoder;
import cloud.rocksdb.server.config.Configuration;
import com.google.common.collect.Maps;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by fafu on 2017/7/10.
 */
public class MasterServer extends AbstractServer {
    private static final Logger log = LoggerFactory.getLogger(MasterServer.class);
    private ServiceDiscover serviceDiscovery;
    private List<String> shardIdList;
    private Map<String,Map<String,DataServerNode>> serverNodeMap ;
    private EventLoopGroup eventLoopGroup;
    private ServerBootstrap bootstrap;

    public MasterServer(Configuration config){
        super(config);
    }

    @Override
    public void doInit() throws Exception {
        serviceDiscovery = new ServiceDiscover(curator,config.getZkServiceDiscoveryPath());
        this.eventLoopGroup = new NioEventLoopGroup(config.getInstanceConcurrency());//设置并发度
        serverNodeMap = Maps.newConcurrentMap();
        this.bootstrap = new ServerBootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(this.host, this.port))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(config.getRequestMaxBodyLength(), 0, 4));
                        ch.pipeline().addLast(new ReplicatorDecoder());
                        ch.pipeline().addLast(new ReplicatorEncoder());
                        ch.pipeline().addLast(new MasterProxyHandler(serverNodeMap));
                    }
                });
    }

    @Override
    public void doShutdown() throws Exception {
        eventLoopGroup.shutdownGracefully(10000,10000, TimeUnit.MICROSECONDS);
        serviceDiscovery.close();
    }

    @Override
    public void doStartup() throws Exception {
        serviceDiscovery.start();
        shardIdList.stream().forEach(shard->{
            try {
                Map<String,ServiceInstance<Container>> containerMap = serviceDiscovery.getAllMap(shard);
                Map<String,DataServerNode> map = containerMap.entrySet().stream().collect(Collectors.toMap(entry->entry.getKey(),entry->of(entry.getValue())));
                serverNodeMap.put(shard,map);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        ChannelFuture future = bootstrap.bind().sync();
        future.channel().closeFuture().sync();
    }


    public DataServerNode of(ServiceInstance<Container> serviceInstance){
        return new DataServerNode(config,serviceInstance);
    }


    public void rebuild(String shard){
        List<ServiceInstance<Container>> instances = serviceDiscovery.getAllInstance(shard);
        if(instances == null||instances.size()==0){
            throw new RuntimeException("No Available Server Found!");
        }

        instances.forEach(instance->{
            if(!serverNodeMap.containsKey(instance.getId())){
                Map<String,DataServerNode> shardMap = serverNodeMap.get(shard);
                if(shardMap == null){
                    shardMap = Maps.newConcurrentMap();
                    serverNodeMap.put(shard,shardMap);
                }
                serverNodeMap.get(shard).put(instance.getId(),of(instance));
            }
        });

        Set<ServiceInstance<Container>> set = new HashSet<>(instances);
        serverNodeMap.forEach((k,v)->{
            if(!set.contains(k)){
                serverNodeMap.remove(k);
            }
        });
    }




}
