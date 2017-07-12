package cloud.rocksdb.server.master;

import cloud.rocksdb.server.config.LifeCycle;

/**
 * Created by fafu on 2017/7/11.
 */
public class Cluster implements LifeCycle{

    public Cluster(){}

    private Status status;

    @Override
    public void init() throws Exception {

    }

    @Override
    public void startup() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }


    public void onShardStarted(){

    }

    public void onShardShutdown(){

    }
}
