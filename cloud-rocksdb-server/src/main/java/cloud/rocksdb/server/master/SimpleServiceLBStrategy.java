package cloud.rocksdb.server.master;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by fafu on 2017/7/19.
 */
public class SimpleServiceLBStrategy extends ServiceLBStrategy {
    private AtomicInteger inc = new AtomicInteger(0);

    public SimpleServiceLBStrategy(ServiceDiscover shardDiscover) {
        super(shardDiscover);
    }

    @Override
    public DataServerNode get(byte[] key) {
        return serviceDiscover.getDataServers().get(inc.getAndIncrement()%serviceDiscover.getDataServers().size());
    }

    @Override
    public DataServerNode get() {
        return get(null);
    }
}
