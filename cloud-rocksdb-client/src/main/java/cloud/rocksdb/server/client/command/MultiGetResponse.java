package cloud.rocksdb.server.client.command;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fafu on 2017/5/31.
 */
@NoArgsConstructor
public class MultiGetResponse extends Response<Map<byte[],byte[]>> {

    private Map<byte[],byte[]> content;
    private int length = -1;
    private byte size = 0;

    public MultiGetResponse(Map<byte[],byte[]> content) {
        if(content != null)
            this.content = content;
            this.size = (byte)content.size();
    }

    public COMMAND_TYPE getType() {
        return COMMAND_TYPE.MULTIGET_RESPONSE;
    }

    @Override
    public void writeBody(ByteBuf out) {
        super.writeBody(out);
        out.writeByte(size);
        content.entrySet().stream().forEach(entry->{
            writeIntHeadContent(entry.getKey(),out);
            writeIntHeadContent(entry.getValue(),out);
        });
    }

    @Override
    protected Command readBody(ByteBuf out) {
        super.readBody(out);
        size = out.readByte();
        content = new HashMap<>(size);
        for(int i = 0;i<size;i++){
            content.put(readIntHeadContent(out),readIntHeadContent(out));
        }
        return this;
    }

    @Override
    public int length() {
        if(length != -1)return length;
        length = 1+content.entrySet().stream().mapToInt(entry->{
            return 8 + entry.getKey().length + (entry.getValue() == null?0:entry.getValue().length);
        }).sum();
        return length;
    }

    @Override
    public Map<byte[],byte[]> getResult() {
        return content;
    }
}
