package cloud.rocksdb.server.data;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;

/**
 * Created by fafu on 2017/7/12.
 */

@Data
@JsonRootName("details")
public class Container {
    private String host;
    private int port;
    private String shard;
}
