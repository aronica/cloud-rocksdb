package cloud.rocksdb.server.client.command;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;

/**
 * Created by fafu on 2017/5/31.
 */
@NoArgsConstructor
public class GetLatestSequenceNumResponse extends Response<Long> {

    private long sequence;

    public GetLatestSequenceNumResponse(long sequence) {
        this.sequence = sequence;
    }

    public COMMAND_TYPE getType() {
        return COMMAND_TYPE.GET_LATEST_SEQ_RESPONSE;
    }

    @Override
    public void writeBody(ByteBuf out) {
        super.writeBody(out);
        out.writeLong(sequence);
    }

    @Override
    protected Command readBody(ByteBuf out) {
        super.readBody(out);
        this.sequence = out.readLong();
        return this;
    }

    @Override
    public int length() {
        return super.length() + 8;
    }

    @Override
    public Long getResult() {
        return sequence;
    }
}
