package cloud.rocksdb.server.db;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by fafu on 2017/6/7.
 */
public class Master extends Node {

    private CopyOnWriteArrayList<Slave> slaves;

    public Master(String host, int port) {
        super(host, port);
    }

    public boolean isMaster() {
        return true;
    }

    public boolean isSlave() {
        return false;
    }

    public void addSlave(Slave slave){
        slaves.add(slave);
    }
}
