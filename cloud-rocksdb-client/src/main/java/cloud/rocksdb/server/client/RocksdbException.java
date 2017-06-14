package cloud.rocksdb.server.client;

/**
 * Created by fafu on 2017/6/6.
 */
public class RocksdbException extends RuntimeException {

    public RocksdbException(){
        super();
    }
    public RocksdbException(String msg){
        super(msg);
    }

    public RocksdbException(Throwable t){
        super(t);
    }
    public RocksdbException(String msg,Throwable t){
        super(msg,t);
    }
}
