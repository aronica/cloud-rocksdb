package cloud.rocksdb.server.partition;

/**
 * Created by fafu on 2017/7/10.
 */
public interface Partitioner {

    public int partition(String key,int size);

}
