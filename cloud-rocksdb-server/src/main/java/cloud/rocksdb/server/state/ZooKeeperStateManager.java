package cloud.rocksdb.server.state;

import com.google.common.collect.Lists;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;

import java.util.Arrays;
import java.util.List;

/**
 * Created by fafu on 2017/6/13.
 */
public class ZooKeeperStateManager implements StateManager,AutoCloseable {
    private CuratorFramework client;
    private LeaderLatch leaderLatch;
    private List<ShardMasterListener> listeners;

    private String path;

    public ZooKeeperStateManager(CuratorFramework client, String path, ShardMasterListener... listeners){
        this.client = client;
        this.path = path;
        this.leaderLatch = new LeaderLatch(client, path);
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
