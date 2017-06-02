package cloud.rocksdb.client;

import java.util.List;
import java.util.Map;

/**
 * Created by fafu on 2017/5/31.
 */
public interface Client<T> {

    public long getSeq()throws Exception;

    public T get(T key)throws Exception;

    public void put(T key,T value)throws Exception;

    public Map<? extends T,? extends T> multiGet(List<? extends T> keys) throws Exception;

    public boolean exist(T key)throws Exception;

    public long getLatestSequenceNum()throws Exception;

//    public Future<T> asyncGet(T key);
//
//    public Future<Void> asyncPut(T key);
//
//    public Future<Map<? extends T,? extends T>> asyncMultiGet(List<? extends T> keys);
//
//    public Future<Boolean> asyncExist(T key);
//
//    public Future<Long> asyncGetLatestSequenceNum();
//
//    public void get(T key, Consumer<T> consumer);
//
//    public void put(T key,T value ,Consumer<T> consumer);
//
//    public void multiGet(List<? extends T> keys,Consumer<Map<? extends T,? extends T>> consumer);
//
//    public void exist(T key,Consumer<T> consumer);
//
//    public void getLatestSequenceNum(T key,Consumer<Long> consumer);
}
