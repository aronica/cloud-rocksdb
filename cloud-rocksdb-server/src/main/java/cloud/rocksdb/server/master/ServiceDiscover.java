package cloud.rocksdb.server.master;

import cloud.rocksdb.server.config.Configuration;
import cloud.rocksdb.server.config.LifeCycle;
import cloud.rocksdb.server.util.JacksonUtil;
import cloud.rocksdb.server.zookeeper.ZKUtils;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by fafu on 2017/7/12.
 */
public class ServiceDiscover implements LifeCycle {
    private static final Logger log = LoggerFactory.getLogger(ServiceDiscover.class);
    private CuratorFramework client;
    private String shardId;
    private Configuration config;
    private PathChildrenCache cache;
    private AtomicReference<Shard> shard = new AtomicReference<>();
    private NodeCache shardNodeCache;
    private ServiceLBStrategy serviceLBStrategy = new SimpleServiceLBStrategy(this);
    private ShardDiscover shardDiscover;
    private volatile DataServerNode master;
    private final ConcurrentHashMap<Container, DataServerNode> serviceProviderMap = new ConcurrentHashMap<>();
    private ArrayBlockingQueue<Pair<Container,DataServerNode>> offLineServers = new ArrayBlockingQueue<Pair<Container, DataServerNode>>(1000);
    private Thread daemon = new Thread(()->{
        while(true){
            Pair<Container,DataServerNode> pair = null;
            try {
                pair = offLineServers.take();
                Container container = pair.getKey();
                DataServerNode dataServerNode = pair.getValue();
                Thread.sleep(3000);
                ChildData cd = cache.getCurrentData(config.getZkShardServiceInstancePath(shardId,pair.getKey().getHost(),pair.getKey().getPort()));
                if(cd == null){
                    serviceProviderMap.remove(container);
                    if (dataServerNode != null)
                        dataServerNode.close();
                }else{
                    container = JacksonUtil.toObject(cd.getData(),Container.class);
                    if(container.getStatus() == 1){
                        //just ignore message.
                        return;
                    }else{
                        //server doesn't recover during last 3s.
                        serviceProviderMap.remove(container);
                        dataServerNode.close();
                    }
                }
            } catch (InterruptedException e) {
                offLineServers.clear();
                return;
            } catch (Exception e) {
                log.error("",e);
            }
        }


    },"ServiceDaemonThread");


    public DataServerNode getOne(byte[] key){
        return serviceLBStrategy.get(key);
    }

    public DataServerNode getOne(){
        return serviceLBStrategy.get();
    }

    public ServiceDiscover(Configuration config,CuratorFramework client ,ShardDiscover shardDiscover, String shardId){
        this.client = client;
        this.shardId = shardId;
        this.config = config;
        this.shardDiscover = shardDiscover;
    }

    public List<DataServerNode> getDataServers(){
        Collection<DataServerNode> servers = serviceProviderMap.values();
        return new ArrayList<>(servers);
    }

    public DataServerNode getMaster(){
        return master;
    }

    //todo Does every shard need a master?
    public synchronized void electShardMaster() throws Exception {
        master = null;
        List<DataServerNode> nodes = getDataServers();
        List<Pair<DataServerNode,Long>> seq = Lists.newArrayList();
        if(nodes != null){
            for(int i = 0;i<nodes.size();i++){
                try {
                    seq.add(Pair.of(nodes.get(i),nodes.get(i).getLatestSequenceNum()));
                } catch (Exception e) {
                    log.error("",e);
                    seq.add(Pair.of(nodes.get(i),-1L));
                }
            }
            seq.sort((i,j)->(int)(i.getValue()-j.getValue()));
            //more than one instance
            if(nodes.size()>1 && seq.get(seq.size()-1).getValue().longValue() > seq.get(0).getValue().longValue()){
                master = seq.get(seq.size()-1).getKey();
            }else if(nodes.size()>1){
                master = seq.get(0).getKey();
            }
            if(master != null){
                Shard old = null;
                if(shardNodeCache.getCurrentData().getData()!=null){
                    try{
                        old = JacksonUtil.toObject(shardNodeCache.getCurrentData().getData(),Shard.class);
                    }catch (Exception e){
                        log.error("",e);
                    }
                }
                Shard shard = new Shard();
                shard.setMasterHost(master.getInstance().getHost());
                shard.setMasterPort(master.getInstance().getPort());
                shard.setShardId(shardId);
                shard.setEpoch(old != null?old.getEpoch():1);
                ZKUtils.setPersistentData(client,config.getZkShardRoot(shardId),JacksonUtil.toJsonAsBytes(shard));
            }
        }
    }

    @Override
    public void startup() throws Exception {
//        this.cache.start();
//        this.shardNodeCache.start();
        daemon.setDaemon(true);
        daemon.start();
    }

    @Override
    public void init() throws Exception {
        ZKUtils.createPersistentPathIfNotExist(client,config.getZkShardRoot(shardId));
        this.shardNodeCache = new NodeCache(client,config.getZkShardRoot(shardId),false);
        this.shardNodeCache.start();
        shardNodeCache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                Shard newShard = JacksonUtil.toObject(shardNodeCache.getCurrentData().getData(),Shard.class);
                if(newShard.getEpoch() != shard.get().getEpoch()){
                    shard.set(newShard);
                }
            }
        });
        this.cache = new PathChildrenCache(client,config.getZkShardServicePath(shardId),true);
        this.cache.start();

        List<String> childrens = this.client.getChildren().forPath(config.getZkShardServicePath(shardId));
        for(String path:childrens){
            byte[] data = client.getData().forPath(config.getZkShardServicePath(shardId)+"/"+path);
            if(data != null){
                Container container = JacksonUtil.toObject(data,Container.class);
                DataServerNode dataServerNode = new DataServerNode(config,container);
                DataServerNode old = serviceProviderMap.putIfAbsent(container,dataServerNode);
                if(old != null){
                    dataServerNode.close();
                }else{
                    dataServerNode.init();
                    dataServerNode.startup();
                    dataServerNode.getStatus().set(DataServerNodeStatus.SERVING);
                }
            }
        }

        cache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                ChildData data = pathChildrenCacheEvent.getData();
                switch (pathChildrenCacheEvent.getType()){
                    case CHILD_ADDED:
                        if(data != null){
                            Container container = JacksonUtil.toObject(data.getData(),Container.class);
                            DataServerNode dataServerNode = new DataServerNode(config,container);
                            DataServerNode old = serviceProviderMap.putIfAbsent(container,dataServerNode);
                            if(old != dataServerNode){
                                dataServerNode.close();
                            }else{
                                dataServerNode.startup();
                            }
                            old.getStatus().set(DataServerNodeStatus.SERVING);
                        }
                        break;
                    case CHILD_REMOVED:
                        if(data != null){
                            Container container = JacksonUtil.toObject(data.getData(),Container.class);
                            DataServerNode dataServerNode = serviceProviderMap.get(container);
                            if(container.getStatus() == 0){//主动断开下线
                                serviceProviderMap.remove(container);
                                dataServerNode.close();
                            }else if(container.getStatus() == 1){//正常情况下节点删除，可能是网络抖动/zookeeper超时等引起的临时非正常掉线
                                offLineServers.add(Pair.of(container,dataServerNode));
                            }
                        }
                        break;
                    case CHILD_UPDATED:
                        if(data != null){
                            Container container = JacksonUtil.toObject(data.getData(),Container.class);
                            if(container.getStatus() == 0){//主动断开下线
                                DataServerNode dataServerNode = serviceProviderMap.get(container);
                                dataServerNode.getStatus().set(DataServerNodeStatus.DOWNING);
                            }
                        }
                        break;
                    default:
                }
            }
        });
    }

    @Override
    public void shutdown() throws Exception {
        if(this.cache != null)
            this.cache.close();
    }
}
