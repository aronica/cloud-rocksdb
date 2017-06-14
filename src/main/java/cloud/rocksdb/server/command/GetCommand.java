package cloud.rocksdb.server.command;

import lombok.NoArgsConstructor;

/**
 * Created by fafu on 2017/5/30.
 */
@NoArgsConstructor
public class GetCommand extends SingleFieldCommand {

    public GetCommand(byte[] content) {
        super(content);
    }

    public COMMAND_TYPE getType() {
        return COMMAND_TYPE.GET;
    }

}
