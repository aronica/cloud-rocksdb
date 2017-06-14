package cloud.rocksdb.server.db;

import lombok.Data;

import java.util.BitSet;

/**
 * Created by fafu on 2017/6/7.
 */
@Data
public abstract class Node implements Cloneable{

    public static final int ALIVE = 1;
    public static final int MOVING = 2;
    public static final int FAILING = -1;
    private String host;
    private int port;
    private volatile int status = 1;
    private BitSet slot;
    private long lastPingSent;
    private long lastPongReceived;

    public Node(String host, int port){
        this.host = host;
        this.port = port;
    }


}
