package cloud.rocksdb.server.data;

import cloud.rocksdb.server.client.command.*;
import cloud.rocksdb.server.client.command.cluster.PongCommand;
import cloud.rocksdb.server.db.RocksDBHolder;
import com.google.common.collect.Lists;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

/**
 * Created by fafu on 2017/5/30.
 */
@Data
@AllArgsConstructor
public class NodeServerHandler extends ChannelInboundHandlerAdapter {
    private RocksDBHolder rocksDBHolder;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Command command = (Command)msg;
        switch (command.getType()){
            case PING:
                ctx.writeAndFlush(new PongCommand());
                break;
            case SEND_SEQ:
//                SendSeqCommand cmd = (SendSeqCommand) command;
                break;
            case MULTIGET:
                MultiGetCommand multiGetCommand = (MultiGetCommand)command;
                Map<byte[], byte[]> val = rocksDBHolder.getResource().multiGet(Lists.newArrayList(multiGetCommand.getKeys()));
                ctx.writeAndFlush(new MultiGetResponse(val));
                break;
            case DELETE:
                DeleteCommand deleteCommand = (DeleteCommand)command;
                rocksDBHolder.getResource().delete(deleteCommand.content);
                ctx.writeAndFlush(new DeleteResponse());
                break;
            case GET_LATEST_SEQ:
//                GetLatestSequenceNumCommand getLatestSequenceNumCommand = (GetLatestSequenceNumCommand)command;
                long seq = rocksDBHolder.getResource().getLatestSequenceNumber();
                ctx.writeAndFlush(new GetLatestSequenceNumResponse(seq));
                break;
            case EXIST:
                ExistCommand existCommand = (ExistCommand)command;
                boolean exist = rocksDBHolder.getResource().keyMayExist(existCommand.content,null);
                ctx.writeAndFlush(new ExistResponse(exist));
                break;
            default:
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelReadComplete");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
