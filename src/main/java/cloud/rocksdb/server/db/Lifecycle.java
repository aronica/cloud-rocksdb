package cloud.rocksdb.server.db;

/**
 * Created by fafu on 2017/6/7.
 */
public interface Lifecycle {
    public void init() throws Exception;

    public void destroy() throws Exception;
}
