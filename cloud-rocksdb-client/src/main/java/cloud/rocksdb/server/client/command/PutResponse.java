package cloud.rocksdb.server.client.command;

/**
 * Created by fafu on 2017/6/2.
 */
public class PutResponse extends Response<Void> {
    @Override
    public Command.COMMAND_TYPE getType() {
        return COMMAND_TYPE.PUT_RESPONSE;
    }
}
