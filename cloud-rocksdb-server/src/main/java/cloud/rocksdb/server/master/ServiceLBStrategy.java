package cloud.rocksdb.server.master;

/**
 * Created by fafu on 2017/7/19.
 */
public abstract class ServiceLBStrategy {
    protected ServiceDiscover serviceDiscover;

    public ServiceLBStrategy(ServiceDiscover shardDiscover){
        this.serviceDiscover = shardDiscover;
    }
    public abstract DataServerNode get(byte[] key);

    public abstract DataServerNode get();
}


