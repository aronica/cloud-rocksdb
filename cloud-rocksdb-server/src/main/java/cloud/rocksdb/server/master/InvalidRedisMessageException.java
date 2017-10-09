package cloud.rocksdb.server.master;

/**
 * Created by fafu on 2017/8/2.
 */
public class InvalidRedisMessageException extends Exception {

    public InvalidRedisMessageException(String message) {
        super(message);
    }


}
