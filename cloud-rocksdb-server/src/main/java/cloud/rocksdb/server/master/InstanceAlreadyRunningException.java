package cloud.rocksdb.server.master;

/**
 * Created by fafu on 2017/6/7.
 */
public class InstanceAlreadyRunningException extends Exception {

    public InstanceAlreadyRunningException(String msg){
        super(msg);
    }

    public InstanceAlreadyRunningException(String msg, Throwable t){
        super(msg,t);
    }
}
