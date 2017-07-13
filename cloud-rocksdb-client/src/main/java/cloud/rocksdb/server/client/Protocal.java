package cloud.rocksdb.server.client;

import cloud.rocksdb.server.client.command.Command;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by fafu on 2017/6/7.
 */
public class Protocal {

    public static Command readCommand(InputStream in) throws IOException {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        buf.writeBytes(in,4);
        buf.markReaderIndex();
        int length = buf.readInt();
        buf.resetReaderIndex();
        buf.writeBytes(in,length);
        return Command.get(buf);
    }

    public static void writeCommand(Command command, OutputStream out) throws IOException {
        int size = command.size();
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(size);
        command.write(buf);
        buf.readBytes(out,size);
    }
}
