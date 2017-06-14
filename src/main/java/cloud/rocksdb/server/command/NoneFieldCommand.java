package cloud.rocksdb.server.command;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;

/**
 * Created by fafu on 2017/5/31.
 */
@NoArgsConstructor
public abstract class NoneFieldCommand extends Command {

    public void writeBody(ByteBuf out) {
    }

    public Command readBody(ByteBuf out) {
        return this;
    }

    public int length() {
        return 0;
    }
}
