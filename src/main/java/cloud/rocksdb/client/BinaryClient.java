package cloud.rocksdb.client;

import cloud.rocksdb.command.ExistCommand;
import cloud.rocksdb.command.GetLatestSequenceNumCommand;
import cloud.rocksdb.command.PutCommand;
import cloud.rocksdb.command.MultiGetCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Created by fafu on 2017/5/31.
 */
public class BinaryClient implements Client<byte[]> {
    private static final Logger log = LoggerFactory.getLogger(BinaryClient.class);
    private ConnectionPool pool;

    public BinaryClient(ConnectionPool pool){
        this.pool = pool;
    }

    @Override
    public long getSeq() {
        return 0;
    }

    @Override
    public byte[] get(byte[] key) {
        return new byte[0];
    }

    @Override
    public void put(byte[] key, byte[] value) throws Exception{
        Connection connection = null;
        try{
            connection = pool.getConnection();
            connection.sendTask(new Task(new PutCommand(key,value),connection));
        } catch (TimeoutException e) {
            log.error("",e);
        } catch (ExecutionException e) {
            log.error("",e);
        }finally {
            pool.returnConnectoin(connection);
        }
    }

    @Override
    public Map<? extends byte[], ? extends byte[]> multiGet(List<? extends byte[]> keys) throws Exception {
        Connection connection = null;
        try{
            connection = pool.getConnection();
            return (Map<? extends byte[], ? extends byte[]>) connection.sendTask(new Task(new MultiGetCommand(keys.toArray(new byte[0][])),connection));
        } catch (TimeoutException e) {
            log.error("",e);
            throw e;
        } catch (ExecutionException e) {
            log.error("",e);
            throw e;
        }finally {
            pool.returnConnectoin(connection);
        }
    }

    @Override
    public boolean exist(byte[] key) throws Exception {
        Connection connection = null;
        try{
            connection = pool.getConnection();
            return (Boolean)(connection.sendTask(new Task(new ExistCommand(key),connection)).getResult());
        } catch (TimeoutException e) {
            log.error("",e);
            throw e;
        } catch (ExecutionException e) {
            log.error("",e);
            throw e;
        }finally {
            pool.returnConnectoin(connection);
        }
    }

    @Override
    public long getLatestSequenceNum()throws Exception {
        Connection connection = null;
        try{
            connection = pool.getConnection();
            return (Long)(connection.sendTask(new Task(new GetLatestSequenceNumCommand(),connection)).getResult());
        } catch (TimeoutException e) {
            log.error("",e);
            throw e;
        } catch (ExecutionException e) {
            log.error("",e);
            throw e;
        }finally {
            pool.returnConnectoin(connection);
        }
    }

}
