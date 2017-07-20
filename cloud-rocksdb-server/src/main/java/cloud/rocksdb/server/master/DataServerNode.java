package cloud.rocksdb.server.master;

import cloud.rocksdb.server.client.BinaryClient;
import cloud.rocksdb.server.client.BinaryClientPool;
import cloud.rocksdb.server.client.Client;
import cloud.rocksdb.server.config.Configuration;
import cloud.rocksdb.server.config.LifeCycle;
import lombok.Data;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by fafu on 2017/7/12.
 */
@Data
public class DataServerNode implements LifeCycle, Client<byte[]>,AutoCloseable{
    private static final Logger logger = LoggerFactory.getLogger(DataServerNode.class);
    private volatile Container instance;
    private BinaryClientPool pool;
    private Configuration config;
    private AtomicReference<DataServerNodeStatus> status = new AtomicReference<>();

    private ExecutorService executor = new ThreadPoolExecutor(4, 4,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(1000000),
            (r)->{
                    return new Thread(r,"ProxyWorkerThread");
                },new ThreadPoolExecutor.AbortPolicy());


    public DataServerNode(Configuration config, Container instance) {
        this.config = config;
        this.instance = instance;
    }

    @Override
    public void init() throws Exception {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(config.getMasterDataMaxTotal());
        poolConfig.setMaxIdle(config.getMasterDataMaxIdle());
        poolConfig.setMinIdle(config.getMasterDataMinIdle());
        pool = new BinaryClientPool(poolConfig,instance.getHost(),instance.getPort());
        status.set(DataServerNodeStatus.INIT);
    }

    @Override
    public void startup() throws Exception {
        status.set(DataServerNodeStatus.SERVING);
    }

    @Override
    public void shutdown() throws Exception {
        status.set(DataServerNodeStatus.DOWNING);
        pool.close();
        pool = null;
        status.set(DataServerNodeStatus.DOWN);
    }

    public Future<byte[]> asyncGet(byte[] key,Callback callback){
        return executor.submit(()->{
            byte[] res = null;
            try {
                res = get(key);
                callback.onSuccess();
            } catch (Exception e) {
                callback.onFail();
            } finally {
                return res;
            }
        });
    }

    public Future<?> asyncPut(byte[] key,byte[] value,Callback callback){
        return executor.submit(()->{
            try {
                put(key,value);
                callback.onSuccess();
            } catch (Exception e) {
                e.printStackTrace();
                callback.onFail();
            }
        });
    }

    public Future<?> asyncExist(byte[] key,Callback callback){
        return executor.submit(()->{
            boolean deleted = false;
            try {
                deleted = exist(key);
                callback.onSuccess();
            } catch (Exception e) {
                e.printStackTrace();
                callback.onFail();
            }finally {
                return deleted;
            }
        });
    }

    public Future<?> asyncDelete(byte[] key,Callback callback){
        return executor.submit(()->{
            boolean exist = false;
            try {
                exist = delete(key);
                callback.onSuccess();
            } catch (Exception e) {
                e.printStackTrace();
                callback.onFail();
            }finally {
                return exist;
            }
        });
    }

    public Future<?> asyncMultiGet(List<? extends byte[]> keys,Callback callback) {
        return executor.submit(new Callable<Map<? extends byte[], ? extends byte[]>>() {
            public Map<? extends byte[], ? extends byte[]> call() throws Exception {
                Map<? extends byte[], ? extends byte[]> ret = null;
                try {
                    ret = multiGet(keys);
                    callback.onSuccess();
                }catch (Exception e){
                    e.printStackTrace();
                    callback.onFail();
                }finally {
                    return ret;
                }
            }
        });
    }

    public Future<?> aysncGetLatestSequenceNum(Callback callback){
        return executor.submit(new Callable<Long>() {
            public Long call() throws Exception {
                long ret = 0;
                try {
                    ret = getLatestSequenceNum();
                    callback.onSuccess();
                }catch (Exception e){
                    e.printStackTrace();
                    callback.onFail();
                }finally {
                    return ret;
                }
            }
        });
    }


    @Override
    public byte[] get(byte[] key) throws Exception {
        if(status.get()!=DataServerNodeStatus.SERVING){
            throw new RuntimeException("DataServerNode is not in serving.");
        }
        BinaryClient client = null;
        boolean broken = false;
        try{
             client = pool.getResource();
            return client.get(key);
        }catch (Exception e){
            logger.error("",e);
            broken = true;
            throw e;
        }finally {
            if(client != null){
                if(!broken){
                    pool.returnResource(client);
                }else{
                    pool.returnBrokenResource(client);
                }
            }
        }
    }

    @Override
    public void put(byte[] key, byte[] value) throws Exception {
        if(status.get()!=DataServerNodeStatus.SERVING){
            throw new RuntimeException("DataServerNode is not in serving.");
        }
        BinaryClient client = null;
        boolean broken = false;
        try{
            client = pool.getResource();
            client.put(key,value);
        }catch (Exception e){
            logger.error("",e);
            broken = true;
            throw e;
        }finally {
            if(client != null){
                if(!broken){
                    pool.returnResource(client);
                }else{
                    pool.returnBrokenResource(client);
                }
            }
        }
    }

    @Override
    public Map<? extends byte[], ? extends byte[]> multiGet(List<? extends byte[]> keys) throws Exception {
        if(status.get()!=DataServerNodeStatus.SERVING){
            throw new RuntimeException("DataServerNode is not in serving.");
        }
        BinaryClient client = null;
        boolean broken = false;
        try{
            client = pool.getResource();
            return client.multiGet(keys);
        }catch (Exception e){
            logger.error("",e);
            broken = true;
            throw e;
        }finally {
            if(client != null){
                if(!broken){
                    pool.returnResource(client);
                }else{
                    pool.returnBrokenResource(client);
                }
            }
        }
    }

    @Override
    public boolean exist(byte[] key) throws Exception {
        if(status.get()!=DataServerNodeStatus.SERVING){
            throw new RuntimeException("DataServerNode is not in serving.");
        }
        BinaryClient client = null;
        boolean broken = false;
        try{
            client = pool.getResource();
            return client.exist(key);
        }catch (Exception e){
            logger.error("",e);
            broken = true;
            throw e;
        }finally {
            if(client != null){
                if(!broken){
                    pool.returnResource(client);
                }else{
                    pool.returnBrokenResource(client);
                }
            }
        }
    }

    @Override
    public boolean delete(byte[] key) throws Exception {
        BinaryClient client = null;
        boolean broken = false;
        try{
            client = pool.getResource();
            return client.delete(key);
        }catch (Exception e){
            logger.error("",e);
            broken = true;
            throw e;
        }finally {
            if(client != null){
                if(!broken){
                    pool.returnResource(client);
                }else{
                    pool.returnBrokenResource(client);
                }
            }
        }
    }

    @Override
    public long getLatestSequenceNum() throws Exception {
        if(status.get()!=DataServerNodeStatus.SERVING){
            throw new RuntimeException("DataServerNode is not in serving.");
        }
        BinaryClient client = null;
        boolean broken = false;
        try{
            client = pool.getResource();
            return client.getLatestSequenceNum();
        }catch (Exception e){
            logger.error("",e);
            broken = true;
            throw e;
        }finally {
            if(client != null){
                if(!broken){
                    pool.returnResource(client);
                }else{
                    pool.returnBrokenResource(client);
                }
            }
        }
    }

    @Override
    public void close() throws Exception {
        shutdown();
    }
}
