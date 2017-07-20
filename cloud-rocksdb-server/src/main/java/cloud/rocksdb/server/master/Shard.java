package cloud.rocksdb.server.master;

import lombok.Data;

/**
 * Created by fafu on 2017/7/18.
 */

@Data
public class Shard {

    private String shardId;
    private String masterHost;
    private int masterPort = 0;
    private int epoch = 0;
}
