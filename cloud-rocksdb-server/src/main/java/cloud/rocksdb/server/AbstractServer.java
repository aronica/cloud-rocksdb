package cloud.rocksdb.server;

import cloud.rocksdb.server.config.Configuration;
import cloud.rocksdb.server.config.LifeCycle;
import cloud.rocksdb.server.util.NetworkInterfaceHelper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;

/**
 * Created by fafu on 2017/7/12.
 */
public abstract class AbstractServer implements LifeCycle {
    protected String host;
    protected int port;
    protected CuratorFramework curator;
    protected Configuration config;

    private void initContainer() {
        this.host = NetworkInterfaceHelper.INSTANCE.getLocalHostAddress();
        this.port = doGetPort();
    }

    protected abstract int doGetPort();

    public AbstractServer(Configuration config){
        this.config = config;
    }

    private void initZookeeper() {
        String zookeeper = config.getZookeeperHost();
        curator = CuratorFrameworkFactory.newClient(zookeeper, 30 * 1000, 3000, new RetryNTimes(10, 1000));
        curator.start();
    }

    @Override
    public void init() throws Exception {
        initContainer();
        initZookeeper();
        doInit();
    }

    public abstract void doInit() throws Exception;

    public abstract void doShutdown()throws Exception;

    public abstract void doStartup()throws Exception;

    @Override
    public void startup() throws Exception {
        doStartup();
    }

    @Override
    public void shutdown() throws Exception {
        doShutdown();
        curator.close();
    }
}
