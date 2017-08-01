package cloud.rocksdb.server.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by fafu on 2017/6/14.
 */
public class Configuration {

    public static final int PING_INTERVAL_DEFAULT = 3000;

    public static final int PING_PONG_NO_RESPONCE_NUM_DEFAULT = 3;

    public static final String ZK_ROOT_DEF = "/zoo";
    public static final String ZK_ROOT_KEY = "zk.root.path";

    public static final String ZK_LEADER_LATCH_DIR_DEF = "leader";
    public static final String ZK_LEADER_LATCH_DIR_KEY = "zk.leader.path";

    public static final String ZK_INSTANCE_ROOT_DEF = "instance";
    public static final String ZK_INSTANCE_ROOT_KEY = "zk.instance.path";

    public static final String ZK_SHARD_ROOT_DEF = "shard";
    public static final String ZK_SHARD_ROOT_KEY = "zk.instance.path";
    private static final String ZK_MASTER_KEY = "zk.master.path";
    private static final String ZK_MASTER_DEF = "master";


    private Map<String,String> properties;

    public Configuration(){
        properties = new HashMap<>();
    }

    public Configuration(File file) throws IOException {
        this(new FileInputStream(file));
    }

    public Configuration(InputStream in) throws IOException {
        Properties properties = new Properties();
        try {
            properties.load(in);
        } catch (IOException e) {
           throw e;
        }finally {
            if(in != null){
                try {
                    in.close();
                } catch (IOException e) {
                    //ignore
                }
            }
        }
    }

    public String getZkRoot(){
        return properties.getOrDefault(ZK_ROOT_KEY,ZK_ROOT_DEF);
    }

    public String getZkLeaderPathDir(){
        return getZkRoot() + "/" + properties.getOrDefault(ZK_LEADER_LATCH_DIR_KEY,ZK_LEADER_LATCH_DIR_DEF);
    }

    public String getZkInstanceRoot(){
        return getZkRoot() + "/" + properties.getOrDefault(ZK_INSTANCE_ROOT_KEY,ZK_INSTANCE_ROOT_DEF);
    }

    public String getUpdateStategy(){
        return "QUORUM";
    }

    public int getDataPort(){
        return Integer.valueOf(properties.getOrDefault("server.data.tcp.port","8000"));
    }

    public int getMasterPort(){
        return Integer.valueOf(properties.getOrDefault("server.server.tcp.port","9000"));
    }

    public int getInstanceConcurrency(){
        return Integer.valueOf(properties.getOrDefault("server.worker.thread.num","4"));
    }

    public int getRequestMaxBodyLength(){
        return Integer.valueOf(properties.getOrDefault("server.tcp.request.max_body_length",String.valueOf(1024*1024*5)));
    }

    public String getZkServiceDiscoveryPath(){
        return getZkRoot() + "/" + properties.getOrDefault(ZK_MASTER_KEY,ZK_MASTER_DEF) + "/" + "service";
    }

    public String getZkShardParent(){
        return getZkRoot() + "/" + properties.getOrDefault(ZK_SHARD_ROOT_KEY,ZK_SHARD_ROOT_DEF);
    }

    public String getZkShardRoot(String shardId){
        return getZkRoot() + "/" + properties.getOrDefault(ZK_SHARD_ROOT_KEY,ZK_SHARD_ROOT_DEF)+"/"+shardId;
    }

    public String getZkShardChildren(String shardId,String child){
        return getZkShardRoot(shardId) + "/" + child;
    }

    public String getZkShardServicePath(String shardId){
        return getZkShardRoot(shardId)+"/"+"service";
    }

    public String getZkShardServiceInstancePath(String shardId,String host,int port){
        return getZkShardRoot(shardId)+"/"+"service/"+host+"_"+port;
    }

    public String get(String key){
        return properties.get(key);
    }

    public String getZookeeperHost(){
        return properties.getOrDefault("cluster.zookeeper.host","127.0.0.1");
    }

    public int getMasterDataMaxTotal(){
        return Integer.valueOf(properties.getOrDefault("cluster.master.data.maxTotal","4"));
    }

    public int getMasterDataMaxIdle(){
        return Integer.valueOf(properties.getOrDefault("cluster.master.data.maxIdle","4"));
    }

    public int getMasterDataMinIdle(){
        return Integer.valueOf(properties.getOrDefault("cluster.master.data.minIdle","2"));
    }
}
