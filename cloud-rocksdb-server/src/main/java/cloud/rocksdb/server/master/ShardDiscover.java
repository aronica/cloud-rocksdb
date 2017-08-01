package cloud.rocksdb.server.master;

import cloud.rocksdb.server.config.Configuration;
import cloud.rocksdb.server.config.LifeCycle;
import cloud.rocksdb.server.zookeeper.ZKUtils;
import com.google.common.collect.Maps;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Created by fafu on 2017/7/17.
 */
public class ShardDiscover implements LifeCycle {
    private CuratorFramework client;
    private PathChildrenCache cache;
    private Configuration config;
    private Map<String,ServiceDiscover> serviceDiscoverMap = Maps.newConcurrentMap();
    private ShardLBStrategy strategy ;
    private LeaderLatch leaderLatch;
    private AtomicBoolean master = new AtomicBoolean(false);
    public List<String> getAllShards(){
        return new ArrayList<>(serviceDiscoverMap.keySet());
    }

    public ServiceDiscover getServiceDiscover(String shardId){
        return serviceDiscoverMap.get(shardId);
    }

    public ShardDiscover(Configuration config,CuratorFramework client,ShardLBStrategy strategy){
        this.client = client;
        this.config = config;
        this.strategy = strategy;
    }

    public boolean isMaster(){
        return master.get();
    }

    public ShardDiscover(Configuration config,CuratorFramework client){
        this(config,client,new SimpleLBStrategy());
    }

    @Override
    public void init() throws Exception {
        ZKUtils.createPersistentPathIfNotExist(client,config.getZkShardParent());
        cache = new PathChildrenCache(client,config.getZkShardParent(),true);
        leaderLatch = new LeaderLatch(client,config.getZkServiceDiscoveryPath());

    }

    public ServiceDiscover get(byte[] key){
        return strategy.get(this,key);
    }


    public Map<ServiceDiscover,List<byte[]>> multiGet(List<byte[]> keys){
        return keys.stream().collect(Collectors.groupingBy(this::get));
    }


    @Override
    public void startup() throws Exception {
        cache.start();
        cache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                switch (event.getType()){
                    case CHILD_ADDED:
                        //todo shard add
                        break;
                    case CHILD_REMOVED:
                        //todo shard remove
                        break;
                    case CHILD_UPDATED:
                        //todo shard delete
                        break;
                }
            }
        });
        List<String>childrens = client.getChildren().forPath(config.getZkShardParent());
        for (String path:childrens) {
            String shardId = path.substring(path.lastIndexOf("/") + 1);
            ServiceDiscover discover = new ServiceDiscover(config, client,this, shardId);
            ServiceDiscover old = serviceDiscoverMap.putIfAbsent(shardId,discover);
            if(old != null){
                discover.shutdown();//
            }else{
                discover.init();
                discover.startup();
            }
        }
        leaderLatch.start();
        leaderLatch.addListener(new LeaderLatchListener() {
            @Override
            public void isLeader() {
                master.set(true);
            }

            @Override
            public void notLeader() {
                master.set(false);
            }
        });
    }

    @Override
    public void shutdown() throws Exception {
        if(cache != null)cache.close();
        leaderLatch.close();
        serviceDiscoverMap.forEach((k,v)->{
            try {
                v.shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
