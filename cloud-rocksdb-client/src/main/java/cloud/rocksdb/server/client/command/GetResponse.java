package cloud.rocksdb.server.client.command;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;

/**
 * Created by fafu on 2017/5/31.
 */
@NoArgsConstructor
public class GetResponse extends Response<byte[]> {

    private byte[] content;

    public GetResponse(byte[] content) {
        this.content = content;
    }

    public COMMAND_TYPE getType() {
        return COMMAND_TYPE.GET_RESPONSE;
    }

    @Override
    public void writeBody(ByteBuf out) {
        super.writeBody(out);
        out.writeBytes(content);
    }

    @Override
    protected Command readBody(ByteBuf out) {
        super.readBody(out);
        this.content = new byte[out.readableBytes()];
        out.readBytes(this.content);
        return this;
    }

    @Override
    public int length() {
        return content == null?super.length():super.length()+content.length;
    }

    @Override
    public byte[] getResult(){
        return content;
    }
}
