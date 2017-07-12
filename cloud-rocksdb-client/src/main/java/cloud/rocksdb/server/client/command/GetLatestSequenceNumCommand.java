package cloud.rocksdb.server.client.command;

/**
 * Created by fafu on 2017/5/31.
 */
public class GetLatestSequenceNumCommand extends NoneFieldCommand {
    public COMMAND_TYPE getType() {
        return COMMAND_TYPE.GET_LATEST_SEQ;
    }
}
