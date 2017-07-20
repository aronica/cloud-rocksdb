package cloud.rocksdb.server.master;

/**
 * Created by fafu on 2017/7/17.
 */
public interface ShardLBStrategy {
    public ServiceDiscover get(ShardDiscover discover, byte[] key);
}
