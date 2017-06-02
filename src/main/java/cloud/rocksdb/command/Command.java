package cloud.rocksdb.command;

import io.netty.buffer.ByteBuf;

/**
 * Created by fafu on 2017/5/30.
 */
public abstract class Command {
    private int seq;
    public abstract void writeBody(ByteBuf out);

    public void write(ByteBuf out){
        writeHeader(out);
        writeBody(out);
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    private void writeHeader(ByteBuf out) {
        out.writeInt(length() + 1 +  4);
        out.writeByte(getType().getVal());
        out.writeInt(seq);
    }

    protected abstract Command readBody(ByteBuf out);

    public void readHeader(ByteBuf in){
        this.seq = in.readInt();
    }

    public Command read(ByteBuf in){
        readHeader(in);
        readBody(in);
        return this;
    }

    public abstract COMMAND_TYPE getType();

    public enum COMMAND_TYPE{
        GET((byte)1),
        PUT((byte)2),
        MULTIGET((byte)3),
        EXIST((byte)4),
        DELETE((byte)5),
        GET_LATEST_SEQ((byte)6),

        GET_RESPONSE((byte)20),
        PUT_RESPONSE((byte)21),
        DELETE_RESPONSE((byte)22),
        EXIST_RESPONSE((byte)23),
        MULTIGET_RESPONSE((byte)24),
        GET_LATEST_SEQ_RESPONSE((byte)25);

        private byte val;
        COMMAND_TYPE(byte val) {
            this.val = val;
        }

        public byte getVal(){return val;}
    }

    public abstract int length();

    public static byte[] readIntHeadContent(ByteBuf buf){
        if(buf == null||buf.readableBytes()<4){
            return null;
        }
        int len = buf.readInt();
        byte[] val = new byte[len];
        buf.readBytes(val);
        return val;
    }

    public static int writeIntHeadContent(byte[] content,ByteBuf buf){
        if(buf == null||content == null||content.length == 0){
            return 0;
        }
        buf.writeInt(content.length);
        buf.writeBytes(content);
        return content.length;
    }

}
