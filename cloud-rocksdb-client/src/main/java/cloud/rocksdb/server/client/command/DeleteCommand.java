package cloud.rocksdb.server.client.command;

import lombok.NoArgsConstructor;

/**
 * Created by fafu on 2017/5/30.
 */
@NoArgsConstructor
public class DeleteCommand extends SingleFieldCommand {

    public DeleteCommand(byte[] content) {
        super(content);
    }

    public COMMAND_TYPE getType() {
        return COMMAND_TYPE.DELETE;
    }



}
