package cloud.rocksdb.server.state;

/**
 * Created by fafu on 2017/6/14.
 */
public interface StateManager {

    public default boolean isMaster(){
        return false;
    }

    public default boolean isSlave(){
        return false;
    }

    public default void addListener(ShardMasterListener listener){

    }
}
