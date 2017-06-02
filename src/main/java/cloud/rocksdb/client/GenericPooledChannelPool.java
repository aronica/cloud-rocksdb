package cloud.rocksdb.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.pool.ChannelHealthChecker;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by fafu on 2017/5/31.
 */
public class GenericPooledChannelPool extends SimpleChannelPool {
    private static final Logger log = LoggerFactory.getLogger(GenericPooledChannelPool.class);
    private GenericObjectPool<Future<Channel>> pool ;
    public GenericPooledChannelPool(Bootstrap bootstrap, ChannelPoolHandler handler, GenericObjectPoolConfig config) {
        super(bootstrap, handler);
        this.pool = new GenericObjectPool(new PooledConnectionFactory(GenericPooledChannelPool.super::acquire,
                ChannelHealthChecker.ACTIVE),config);
    }

    public GenericPooledChannelPool(Bootstrap bootstrap, ChannelPoolHandler handler, ChannelHealthChecker healthCheck, GenericObjectPoolConfig config) {
        super(bootstrap, handler, healthCheck);
        this.pool = new GenericObjectPool(new PooledConnectionFactory(GenericPooledChannelPool.super::acquire,
                healthCheck),config);

    }

    public GenericPooledChannelPool(Bootstrap bootstrap, ChannelPoolHandler handler, ChannelHealthChecker healthCheck, boolean releaseHealthCheck, GenericObjectPoolConfig config) {
        super(bootstrap, handler, healthCheck, releaseHealthCheck);
        this.pool = new GenericObjectPool(new PooledConnectionFactory(GenericPooledChannelPool.super::acquire,
                healthCheck),config);
    }


    @Override
    public Future<Channel> acquire(Promise<Channel> promise) {
        try {
            return pool.borrowObject();
        } catch (Exception e) {
            log.error("",e);
            promise.setFailure(e);
        }
        return promise;
    }

    @Override
    public Future<Void> release(Channel channel, Promise<Void> promise) {
//        pool.returnObject();
        //todo
        return null;
    }

    @Override
    public void close() {
        pool.close();
    }
}
