package cloud.rocksdb.server.state;

/**
 * Created by fafu on 2017/6/14.
 */
public interface ShardMasterListener {
    public default void becomeMaster() {};

    public default void loseMaster() {};
}
