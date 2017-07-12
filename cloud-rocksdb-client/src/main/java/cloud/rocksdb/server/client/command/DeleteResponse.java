package cloud.rocksdb.server.client.command;

/**
 * Created by fafu on 2017/6/2.
 */
public class DeleteResponse extends Response<Boolean> {
    @Override
    public COMMAND_TYPE getType() {
        return COMMAND_TYPE.DELETE_RESPONSE;
    }
}
