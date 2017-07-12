package cloud.rocksdb.server.client.command.cluster;

import cloud.rocksdb.server.client.command.SingleFieldCommand;
import io.netty.buffer.ByteBufAllocator;
import lombok.Data;

/**
 * Created by fafu on 2017/6/14.
 */
@Data
public class SendSeqCommand extends SingleFieldCommand {
    private long seq;

    public SendSeqCommand(long seq) {
        super(ByteBufAllocator.DEFAULT.buffer(8).writeLong(seq).array());
        this.seq = seq;
    }

    @Override
    public COMMAND_TYPE getType() {
        return COMMAND_TYPE.SEND_SEQ;
    }
}
