package cloud.rocksdb.client;

import java.util.List;
import java.util.Map;

/**
 * Created by fafu on 2017/5/31.
 */
public interface Client<T> {

//    public long getSeq()throws Exception;

    public T get(T key)throws Exception;

    public void put(T key, T value)throws Exception;

    public Map<? extends T,? extends T> multiGet(List<? extends T> keys) throws Exception;

    public boolean exist(T key)throws Exception;

    public long getLatestSequenceNum()throws Exception;

}
