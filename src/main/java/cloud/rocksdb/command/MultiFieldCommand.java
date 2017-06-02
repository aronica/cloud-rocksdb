package cloud.rocksdb.command;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.List;

/**
 * Created by fafu on 2017/5/31.
 */
@NotThreadSafe
@NoArgsConstructor
public abstract class MultiFieldCommand extends Command{
    protected byte[][] content;
    private int length = -1;

    public MultiFieldCommand(byte[]...content){
        this.content = content;
    }

    public int length(){
        if(content == null||content.length == 0)return 0;
        if(length>-1){
            return length;
        }
        for(byte[] con:content){
            length += con.length;
        }
        return length;
    }

    public void writeBody(ByteBuf out) {
        for(byte[] con:content){
            length += super.writeIntHeadContent(con,out);
        }
    }
    @Override
    public Command readBody(ByteBuf out) {
        List<byte[]> tmp = Lists.newArrayList();
        while(out.readableBytes()>=4){
            tmp.add(readIntHeadContent(out));
        }
        this.content = tmp.toArray(new byte[][]{});
        return this;
    }
}
