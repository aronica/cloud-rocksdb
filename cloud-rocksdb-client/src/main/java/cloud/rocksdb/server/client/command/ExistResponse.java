package cloud.rocksdb.server.client.command;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;

/**
 * Created by fafu on 2017/5/31.
 */
@NoArgsConstructor
public class ExistResponse extends Response<Boolean> {

    private boolean content;

    public ExistResponse(boolean content) {
        this.content = content;
    }

    public COMMAND_TYPE getType() {
        return COMMAND_TYPE.EXIST_RESPONSE;
    }

    @Override
    public void writeBody(ByteBuf out) {
        super.writeBody(out);
        out.writeBoolean(content);
    }

    @Override
    protected Command readBody(ByteBuf out) {
        super.readBody(out);
        this.content = out.readBoolean();
        return this;
    }

    @Override
    public int length() {
        return super.length() + 1;
    }

    @Override
    public Boolean getResult() {
        return content;
    }
}
