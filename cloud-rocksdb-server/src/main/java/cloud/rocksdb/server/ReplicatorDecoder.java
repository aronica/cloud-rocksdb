package cloud.rocksdb.server;

import cloud.rocksdb.server.client.command.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Created by fafu on 2017/5/30.
 */
public class ReplicatorDecoder extends ByteToMessageDecoder {

    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
//        in.markReaderIndex();
        int len = in.readInt();
        byte command = in.readByte();
        if(command == Command.COMMAND_TYPE.GET.getVal()){
            out.add(new GetCommand().read(in));
        }else if(command == Command.COMMAND_TYPE.DELETE.getVal()){
            out.add(new DeleteCommand().read(in));
        }else if(command == Command.COMMAND_TYPE.PUT.getVal()){
            out.add(new PutCommand().read(in));
        }else if(command == Command.COMMAND_TYPE.EXIST.getVal()){
            out.add(new ExistCommand().read(in));
        }else if(command == Command.COMMAND_TYPE.MULTIGET.getVal()) {
            out.add(new MultiGetCommand().read(in));
        }else if((command == Command.COMMAND_TYPE.GET_LATEST_SEQ.getVal())){
            out.add(new GetLatestSequenceNumCommand().read(in));
        }else if(command == Command.COMMAND_TYPE.PUT_RESPONSE.getVal()){
            out.add(new PutResponse().read(in));
        }else if(command == Command.COMMAND_TYPE.GET_RESPONSE.getVal()) {
            out.add(new GetResponse().read(in));
        }else if(command == Command.COMMAND_TYPE.GET_LATEST_SEQ_RESPONSE.getVal()){
            out.add(new GetLatestSequenceNumResponse().read(in));
        } else{
            throw new RuntimeException("Unknown command found.");
        }
    }
}
