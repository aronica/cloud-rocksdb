package cloud.rocksdb.server.client.command;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;

/**
 * Created by fafu on 2017/5/31.
 */
@NoArgsConstructor
public abstract class SingleFieldCommand extends Command{
    public byte[] content;

    public SingleFieldCommand(byte[] content){
        this.content = content;
    }

    public int length(){
        return content == null ? 0:content.length;
    }

    public void writeBody(ByteBuf out) {
        out.writeBytes(content);
    }

    public Command readBody(ByteBuf out) {
        content = new byte[out.readableBytes()];
        out.readBytes(content);
        return this;
    }

    public byte[] getKey(){
        return content;
    }
}
