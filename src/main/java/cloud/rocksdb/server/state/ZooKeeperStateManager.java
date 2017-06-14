package cloud.rocksdb.server.state;

import com.google.common.collect.Lists;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.retry.RetryNTimes;

import java.util.Arrays;
import java.util.List;

/**
 * Created by fafu on 2017/6/13.
 */
public class ZooKeeperStateManager implements AutoCloseable {
    private CuratorFramework client;
    private String id;
    private LeaderLatch leaderLatch;
    private List<ShardMasterListener> listeners ;

    public ZooKeeperStateManager(String zk, String id, ShardMasterListener... listeners){
        this.client = CuratorFrameworkFactory.newClient(zk, new RetryNTimes(10,3000));
        this.leaderLatch = new LeaderLatch(client, id);
        this.id = id;
        this.listeners = listeners == null? Lists.newArrayList(): Arrays.asList(listeners);
        this.leaderLatch.addListener(new LeaderLatchListener() {
            @Override
            public void isLeader() {
                if(ZooKeeperStateManager.this.listeners != null){
                    ZooKeeperStateManager.this.listeners.forEach((listener)->{
                        listener.becomeMaster();
                    });
                }
            }

            @Override
            public void notLeader() {
                if(ZooKeeperStateManager.this.listeners != null){
                    ZooKeeperStateManager.this.listeners.forEach((listener)->{
                        listener.loseMaster();
                    });
                }
            }
        });
    }

    @Override
    public void close() throws Exception {
        if(leaderLatch != null)leaderLatch.close();
    }
}
