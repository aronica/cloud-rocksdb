package cloud.rocksdb.server;

import cloud.rocksdb.command.*;
import com.google.common.collect.Lists;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import cloud.rocksdb.db.RocksDBHolder;

import java.util.Map;

/**
 * Created by fafu on 2017/5/30.
 */
public class ServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("channelRead");
        Command command = (Command)msg;
        switch (command.getType()){
            case GET:
                byte[] ret = RocksDBHolder.getInstance().getResource().get(((GetCommand)command).content);
                ctx.writeAndFlush(new GetResponse(ret));
                break;
            case PUT:
                PutCommand cmd = (PutCommand) command;
                RocksDBHolder.getInstance().getResource().put(cmd.getKey(),cmd.getValue());
                ctx.writeAndFlush(new PutResponse());
                break;
            case MULTIGET:
                MultiGetCommand multiGetCommand = (MultiGetCommand)command;
                Map<byte[], byte[]> val = RocksDBHolder.getInstance().getResource().multiGet(Lists.newArrayList(multiGetCommand.getKeys()));
                ctx.writeAndFlush(new MultiGetResponse(val));
                break;
            case DELETE:
                DeleteCommand deleteCommand = (DeleteCommand)command;
                RocksDBHolder.getInstance().getResource().delete(deleteCommand.content);
                ctx.writeAndFlush(new DeleteResponse());
                break;
            case GET_LATEST_SEQ:
                GetLatestSequenceNumCommand getLatestSequenceNumCommand = (GetLatestSequenceNumCommand)command;
                long seq = RocksDBHolder.getInstance().getResource().getLatestSequenceNumber();
                ctx.writeAndFlush(new GetLatestSequenceNumResponse(seq));
                break;
            case GET_RESPONSE:
                break;
            case PUT_RESPONSE:
                break;
            case EXIST:
                ExistCommand existCommand = (ExistCommand)command;
                boolean exist = RocksDBHolder.getInstance().getResource().keyMayExist(existCommand.content,null);
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
