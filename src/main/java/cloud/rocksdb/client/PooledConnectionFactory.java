package cloud.rocksdb.client;

import io.netty.bootstrap.Bootstrap;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by fafu on 2017/5/31.
 */

@NoArgsConstructor
@AllArgsConstructor
public class PooledConnectionFactory implements PooledObjectFactory<Connection> {
    private static final Logger log = LoggerFactory.getLogger(PooledConnectionFactory.class);

    private Bootstrap bootstrap;

    @Override
    public PooledObject<Connection> makeObject() throws Exception {
        return bootstrap.connect();
    }

    @Override
    public void destroyObject(PooledObject<Connection> p) throws Exception {

    }

    @Override
    public boolean validateObject(PooledObject<Connection> p) {
        return false;
    }

    @Override
    public void activateObject(PooledObject<Connection> p) throws Exception {

    }

    @Override
    public void passivateObject(PooledObject<Connection> p) throws Exception {

    }
}
