package cloud.rocksdb.server.client;

/**
 * Created by fafu on 2017/6/6.
 */
public class RocksdbConnectionException extends RocksdbException {

    public RocksdbConnectionException(){
        super();
    }
    public RocksdbConnectionException(String msg){
        super(msg);
    }

    public RocksdbConnectionException(Throwable t){
        super(t);
    }
    public RocksdbConnectionException(String msg, Throwable t){
        super(msg,t);
    }
}
