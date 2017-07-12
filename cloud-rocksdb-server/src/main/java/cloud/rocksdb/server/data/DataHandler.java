package cloud.rocksdb.server.data;

import cloud.rocksdb.server.client.command.*;
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
public class DataHandler extends ChannelInboundHandlerAdapter {
    private RocksDBHolder rocksDBHolder;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Command command = (Command)msg;
        switch (command.getType()){
            case GET:
                byte[] ret = rocksDBHolder.getResource().get(((GetCommand)command).content);
                ctx.writeAndFlush(new GetResponse(ret));
                break;
            case PUT:
                PutCommand cmd = (PutCommand) command;
                rocksDBHolder.getResource().put(cmd.getKey(),cmd.getValue());
                ctx.writeAndFlush(new PutResponse());
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
                GetLatestSequenceNumCommand getLatestSequenceNumCommand = (GetLatestSequenceNumCommand)command;
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
