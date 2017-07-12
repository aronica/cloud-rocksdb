package cloud.rocksdb.server.master;

import cloud.rocksdb.server.client.BinaryClient;
import cloud.rocksdb.server.client.BinaryClientPool;
import cloud.rocksdb.server.client.Client;
import cloud.rocksdb.server.config.Configuration;
import cloud.rocksdb.server.config.LifeCycle;
import lombok.Data;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.curator.x.discovery.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by fafu on 2017/7/12.
 */
@Data
public class DataServerNode implements LifeCycle, Client<byte[]>,AutoCloseable{
    private static final Logger logger = LoggerFactory.getLogger(DataServerNode.class);
    private ServiceInstance<Container> instance;
    private BinaryClientPool pool;
    private Configuration config;

    public DataServerNode(Configuration config, ServiceInstance<Container> instance) {
        this.config = config;
    }

    @Override
    public void init() throws Exception {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(config.getMasterDataMaxTotal());
        poolConfig.setMaxIdle(config.getMasterDataMaxIdle());
        poolConfig.setMinIdle(config.getMasterDataMinIdle());
        pool = new BinaryClientPool(poolConfig,instance.getAddress(),instance.getPort());
    }

    @Override
    public void startup() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {
        pool.close();
        pool = null;
    }


    @Override
    public byte[] get(byte[] key) throws Exception {
        try(BinaryClient client = pool.getResource()){
            return client.get(key);
        }catch (Exception e){
            logger.error("",e);
            throw e;
        }
    }

    @Override
    public void put(byte[] key, byte[] value) throws Exception {
        try(BinaryClient client = pool.getResource()){
            client.put(key,value);
        }catch (Exception e){
            logger.error("",e);
            throw e;
        }
    }

    @Override
    public Map<? extends byte[], ? extends byte[]> multiGet(List<? extends byte[]> keys) throws Exception {
        try(BinaryClient client = pool.getResource()){
            return client.multiGet(keys);
        }catch (Exception e){
            logger.error("",e);
            throw e;
        }
    }

    @Override
    public boolean exist(byte[] key) throws Exception {
        try(BinaryClient client = pool.getResource()){
            return client.exist(key);
        }catch (Exception e){
            logger.error("",e);
            throw e;
        }
    }

    @Override
    public long getLatestSequenceNum() throws Exception {
        try(BinaryClient client = pool.getResource()){
            return client.getLatestSequenceNum();
        }catch (Exception e){
            logger.error("",e);
            throw e;
        }
    }

    @Override
    public void close() throws Exception {
        shutdown();
    }
}
