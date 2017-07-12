package cloud.rocksdb.server.client.command;

import io.netty.buffer.ByteBuf;

/**
 * Created by fafu on 2017/5/31.
 */
public abstract class Response<V> extends Command {
    private byte code;
    private byte[] exe;

    public Response(){
        this.code = 0;
    }
    public Response(byte code, byte[] exe) {
        this.code = code;
        this.exe = exe;
    }

    public void writeBody(ByteBuf out) {
        out.writeByte(code);
        if(exe != null){
            out.writeByte(exe.length);
            out.writeBytes(exe);
        }else{
            out.writeByte((byte)0);
        }
    }

    protected Command readBody(ByteBuf in) {
        this.code = in.readByte();
        byte exeLen = in.readByte();
        if(exeLen > 0){
            this.exe = new byte[exeLen];
            in.writeBytes(exe);
        }
        return this;
    }

    public int length() {
        return 1 +  (exe == null?1:exe.length+1);
    }

    public V getResult(){
        return null;
    }
}
