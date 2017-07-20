package cloud.rocksdb.server.master;

/**
 * Created by fafu on 2017/7/19.
 */
public interface Callback {

    public void onSuccess();

    public default void onFail(){
        onSuccess();
    }
}
