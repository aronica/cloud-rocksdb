package cloud.rocksdb.db;

import cloud.rocksdb.client.RocksDBClient;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by fafu on 2017/6/7.
 */
public class Master extends Node {

    private RocksDBClient rocksDBClient;
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
