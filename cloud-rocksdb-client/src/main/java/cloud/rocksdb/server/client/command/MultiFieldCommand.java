package cloud.rocksdb.server.client.command;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fafu on 2017/5/31.
 */
@NoArgsConstructor
public abstract class MultiFieldCommand extends Command{
    protected byte[][] content;
    private int length = 0;

    public MultiFieldCommand(byte[]...content){
        this.content = content;
    }

    public int length(){
        if(content == null||content.length == 0)return 0;
        if(length>0){
            return length;
        }
        int len = 0;
        for(byte[] con:content){
            len += con.length + 4;
        }
        length = len;
        return length;
    }

    public void writeBody(ByteBuf out) {
        for(byte[] con:content){
            super.writeIntHeadContent(con,out);
        }
    }
    @Override
    public Command readBody(ByteBuf out) {
        List<byte[]> tmp = new ArrayList<>();
        while(out.readableBytes()>=4){
            tmp.add(readIntHeadContent(out));
        }
        this.content = tmp.toArray(new byte[][]{});
        return this;
    }
}
