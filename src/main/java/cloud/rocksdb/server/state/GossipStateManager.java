package cloud.rocksdb.server.state;

/**
 * Created by fafu on 2017/6/14.
 */
public class GossipStateManager implements StateManager {


    @Override
    public boolean isMaster() {
        return false;
    }

    @Override
    public boolean isSlave() {
        return false;
    }

    @Override
    public void addListener(ShardMasterListener listener) {

    }
}
