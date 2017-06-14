package cloud.rocksdb.server.client;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * Created by fafu on 2017/6/9.
 */
public class RocksdbBinaryClientPool extends Pool<BinaryClient> {

    public RocksdbBinaryClientPool() {
    }

    public RocksdbBinaryClientPool(GenericObjectPoolConfig poolConfig, String host,int port) {
        super(poolConfig, new BinaryClientPooledObjectFactory());
    }

    private static class BinaryClientPooledObjectFactory implements PooledObjectFactory<BinaryClient>{
        private String host;
        private int port;
        @Override
        public PooledObject<BinaryClient> makeObject() throws Exception {
            return new DefaultPooledObject<>(new BinaryClient(host,port));
        }

        @Override
        public void destroyObject(PooledObject<BinaryClient> p) throws Exception {
            assert p != null && p.getObject() != null :"Target object to destroy is invalid.";
            p.getObject().close();
        }

        @Override
        public boolean validateObject(PooledObject<BinaryClient> p) {
            assert p != null && p.getObject() != null :"Target object to destroy is invalid.";
            return false;
        }

        @Override
        public void activateObject(PooledObject<BinaryClient> p) throws Exception {

        }

        @Override
        public void passivateObject(PooledObject<BinaryClient> p) throws Exception {

        }
    }
}
