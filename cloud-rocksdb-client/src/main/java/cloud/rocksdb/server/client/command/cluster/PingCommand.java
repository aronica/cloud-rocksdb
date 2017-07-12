package cloud.rocksdb.server.client.command.cluster;

import cloud.rocksdb.server.client.command.NoneFieldCommand;
import lombok.Data;

/**
 * Created by fafu on 2017/6/14.
 */
@Data
public class PingCommand extends NoneFieldCommand {
    @Override
    public COMMAND_TYPE getType() {
        return COMMAND_TYPE.PING;
    }
}
