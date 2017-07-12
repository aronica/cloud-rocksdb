package cloud.rocksdb.server.client;

import cloud.rocksdb.server.client.command.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by fafu on 2017/5/31.
 */
public class BinaryClient extends Connection implements Client<byte[]> ,AutoCloseable{

    private static final Logger log = LoggerFactory.getLogger(BinaryClient.class);
    private int command = 0;

    public BinaryClient(String host) {
        super(host);
    }

    public BinaryClient(String host, int port) {
        super(host, port);
    }

    @Override
    public byte[] get(byte[] key) throws Exception {
        sendCommand(new GetCommand(key));
        command ++;
        Response<?> response = readCommand();
        command --;
        if(response instanceof GetResponse){
            return ((GetResponse)response).getResult();
        }
        close();//close connection since invalid request.
        throw new RocksdbException("Invalid response.");
    }

    @Override
    public void put(byte[] key, byte[] value) throws Exception {
        sendCommand(new PutCommand(key,value));
        command ++;
        Response<?> response = readCommand();
        command --;
        if(response instanceof PutResponse){
            return;
        }
        close();//close connection since invalid request.
        throw new RocksdbException("Invalid response.");
    }
    @Override
    public Map<? extends byte[], ? extends byte[]> multiGet(List<? extends byte[]> keys) throws Exception {
        sendCommand(new MultiGetCommand(keys.toArray(new byte[0][])));
        command ++;
        Response<?> response = readCommand();
        command --;
        if(response instanceof MultiGetResponse){
            return ((MultiGetResponse)response).getResult();
        }
        close();//close connection since invalid request.
        throw new RocksdbException("Invalid response.");
    }

    @Override
    public boolean exist(byte[] key) throws Exception {
        sendCommand(new ExistCommand(key));
        command ++;
        Response<?> response = readCommand();
        command --;
        if(response instanceof ExistResponse){
            return ((ExistResponse)response).getResult();
        }
        close();//close connection since invalid request.
        throw new RocksdbException("Invalid response.");
    }
    @Override
    public long getLatestSequenceNum() throws Exception {
        sendCommand(new GetLatestSequenceNumCommand());
        command ++;
        Response<?> response = readCommand();
        command --;
        if(response instanceof GetLatestSequenceNumResponse){
            return ((GetLatestSequenceNumResponse)response).getResult();
        }
        close();//close connection.
        throw new RocksdbException("Invalid response.");
    }

    @Override
    public void close() throws Exception {
        try {
            super.close();
        }catch (Exception e){
            log.error("",e);
        }
    }
}
