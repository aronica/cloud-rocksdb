package cloud.rocksdb.server.client;

import cloud.rocksdb.server.command.GetLatestSequenceNumCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    @Override
    public long ping() {
        return 0;
    }

}
