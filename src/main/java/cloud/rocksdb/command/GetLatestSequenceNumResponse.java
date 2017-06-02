package cloud.rocksdb.command;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;

/**
 * Created by fafu on 2017/5/31.
 */
@NoArgsConstructor
public class GetLatestSequenceNumResponse extends Response<Long> {

    private long seq;

    public GetLatestSequenceNumResponse(long seq) {
        this.seq = seq;
    }

    public COMMAND_TYPE getType() {
        return COMMAND_TYPE.GET_LATEST_SEQ_RESPONSE;
    }

    @Override
    public void writeBody(ByteBuf out) {
        super.writeBody(out);
        out.writeLong(seq);
    }

    @Override
    protected Command readBody(ByteBuf out) {
        super.readBody(out);
        this.seq = out.readLong();
        return this;
    }

    @Override
    public int length() {
        return super.length() + 8;
    }

    @Override
    public Long getResult() {
        return seq;
    }
}
