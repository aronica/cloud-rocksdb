package cloud.rocksdb.server.master;

import java.util.zip.CRC32;

/**
 * Created by fafu on 2017/7/17.
 */
public class SimpleLBStrategy implements ShardLBStrategy {
    @Override
    public ServiceDiscover get(ShardDiscover discover, byte[] key) {
        if(discover.getAllShards()==null||discover.getAllShards().size() == 0){
            throw new RuntimeException("No Available Shard Found!");
        }
        CRC32 crc = new CRC32();
        crc.update(key);
        return discover.getServiceDiscover(discover.getAllShards().get((int)(crc.getValue() % discover.getAllShards().size())));
    }
}
