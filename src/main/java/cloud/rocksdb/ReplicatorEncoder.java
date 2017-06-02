package cloud.rocksdb;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import cloud.rocksdb.command.Command;

/**
 * Created by fafu on 2017/5/30.
 */
public class ReplicatorEncoder extends MessageToByteEncoder<Command> {

    protected void encode(ChannelHandlerContext ctx, Command msg, ByteBuf out) throws Exception {
        msg.writeBody(out);
    }
}
