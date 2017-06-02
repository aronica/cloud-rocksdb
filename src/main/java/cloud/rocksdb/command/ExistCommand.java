package cloud.rocksdb.command;

import lombok.NoArgsConstructor;

/**
 * Created by fafu on 2017/5/30.
 */
@NoArgsConstructor
public class ExistCommand extends SingleFieldCommand {

    public ExistCommand(byte[] content) {
        super(content);
    }
    public COMMAND_TYPE getType() {
        return COMMAND_TYPE.EXIST;
    }
}
