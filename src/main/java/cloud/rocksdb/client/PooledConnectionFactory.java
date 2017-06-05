package cloud.rocksdb.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.pool.ChannelHealthChecker;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.util.AttributeKey;
import lombok.NoArgsConstructor;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by fafu on 2017/5/31.
 */

@NoArgsConstructor
public class PooledConnectionFactory implements PooledObjectFactory<Connection> {
    private static final Logger log = LoggerFactory.getLogger(PooledConnectionFactory.class);
    private static final AttributeKey<PooledConnectionFactory> POOL_KEY = AttributeKey.newInstance("channelPool");

    private Bootstrap bootstrap;
    private ChannelPoolHandler handler;
    private ChannelHealthChecker healthCheck;

    public PooledConnectionFactory(Bootstrap bootstrap,final ChannelPoolHandler handler,ChannelHealthChecker healthCheck){
        assert bootstrap != null: "Bootstrap shoud not be null";
        assert handler != null: "ChannelPoolHander should not be null";
        assert healthCheck != null: "HealthChecker should not be null";
        this.bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                assert ch.eventLoop().inEventLoop();
                handler.channelCreated(ch);
            }
        });
    }
//    /**
//     * Bootstrap a new {@link Channel}. The default implementation uses {@link Bootstrap#connect()}, sub-classes may
//     * override this.
//     * <p>
//     * The {@link Bootstrap} that is passed in here is cloned via {@link Bootstrap#clone()}, so it is safe to modify.
//     */
//    protected ChannelFuture connectChannel(Bootstrap bs) {
//        return bs.connect();
//    }
//
//    private void notifyConnect(ChannelFuture future, Promise<Channel> promise) {
//        if (future.isSuccess()) {
//            Channel channel = future.channel();
//            if (!promise.trySuccess(channel)) {
//                // Promise was completed in the meantime (like cancelled), just release the channel again
//                release(channel);
//            }
//        } else {
//            promise.tryFailure(future.cause());
//        }
//    }
//
//    private void doHealthCheck(final Channel ch, final Promise<Channel> promise) {
//        assert ch.eventLoop().inEventLoop();
//
//        Future<Boolean> f = healthCheck.isHealthy(ch);
//        if (f.isDone()) {
//            notifyHealthCheck(f, ch, promise);
//        } else {
//            f.addListener(new FutureListener<Boolean>() {
//                @Override
//                public void operationComplete(Future<Boolean> future) throws Exception {
//                    notifyHealthCheck(future, ch, promise);
//                }
//            });
//        }
//    }
//
//    @Override
//    public PooledObject<Connection> makeObject() throws Exception {
//        Channel ch = null;
//        try {
//            // No Channel left in the pool bootstrap a new Channel
//            Bootstrap bs = bootstrap.clone();
//            bs.attr(POOL_KEY, this);
//            ChannelFuture f = connectChannel(bs);
//            Promise<Channel> promise = bootstrap.config().group().next().<Channel>newPromise();
//            if (f.isDone()) {
//                notifyConnect(f, promise);
//            } else {
//                f.addListener(new ChannelFutureListener() {
//                    @Override
//                    public void operationComplete(ChannelFuture future) throws Exception {
//                        notifyConnect(future, promise);
//                    }
//                });
//            }
//            EventLoop loop = ch.eventLoop();
//            if (loop.inEventLoop()) {
//                doHealthCheck(ch, promise);
//            } else {
//                loop.execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        doHealthCheck(ch, promise);
//                    }
//                });
//            }
//        } catch (Throwable cause) {
//            promise.tryFailure(cause);
//        }
//        log.debug("New channel is created");
//        return new DefaultPooledObject<>(new Connection(channel));
//    }

    @Override
    public PooledObject<Connection> makeObject() throws Exception {
        return null;
    }

    @Override
    public void destroyObject(PooledObject<Connection> p) throws Exception {
        Connection connection = p.getObject();
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
