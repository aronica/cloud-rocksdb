package cloud.rocksdb.server.master;

import cloud.rocksdb.server.config.LifeCycle;

/**
 * Created by fafu on 2017/7/10.
 */


public abstract class AbstractContainer extends Container implements LifeCycle{
    private String shardId;
    public AbstractContainer(String shardId){
    }

    @Override
    public void init() {
        //do nothing
    }

    @Override
    public void startup() {

    }

    @Override
    public void shutdown() {
        //do nothing
    }

}
