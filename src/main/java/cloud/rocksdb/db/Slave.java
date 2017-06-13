package cloud.rocksdb.db;

import cloud.rocksdb.client.RocksDBClient;

/**
 * Created by fafu on 2017/6/7.
 */
public class Slave extends Node {

    private RocksDBClient rocksDBClient;
    private Master master;
    private volatile boolean connected = false;

    public Slave(String host, int port) {
        super(host, port);
    }

    public boolean isMaster() {
        return false;
    }

    public boolean isSlave() {
        return true;
    }

    public void setMaster(){
        this.master = master;
    }

    public void connectMaster(){
        if(connected)return;
    }
}
