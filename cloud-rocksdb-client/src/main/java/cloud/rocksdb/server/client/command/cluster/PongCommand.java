package cloud.rocksdb.server.client.command.cluster;

import cloud.rocksdb.server.client.command.NoneFieldCommand;

/**
 * Created by fafu on 2017/6/14.
 */
public class PongCommand extends NoneFieldCommand {
    @Override
    public COMMAND_TYPE getType() {
        return COMMAND_TYPE.PONG;
    }
}
