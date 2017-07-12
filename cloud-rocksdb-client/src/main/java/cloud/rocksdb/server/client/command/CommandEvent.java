package cloud.rocksdb.server.client.command;

import io.netty.buffer.ByteBuf;

/**
 * Created by fafu on 2017/6/1.
 */
public class CommandEvent extends Command {
    private Command internal;
    @Override
    public void writeBody(ByteBuf out) {
        internal.writeBody(out);
    }

    @Override
    protected Command readBody(ByteBuf out) {
        return internal.readBody(out);
    }

    @Override
    public COMMAND_TYPE getType() {
        return internal.getType();
    }

    @Override
    public int length() {
        return internal.length();
    }

    public void setInternal(Command internal){
        this.internal = internal;
    }
}
