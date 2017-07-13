package cloud.rocksdb.server.client.command;

import io.netty.buffer.ByteBuf;

/**
 * Created by fafu on 2017/5/30.
 */
public abstract class Command {
    private int transactionId;

    public abstract void writeBody(ByteBuf out);

    public void write(ByteBuf out) {
        writeHeader(out);
        writeBody(out);
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    private void writeHeader(ByteBuf out) {
        out.writeInt(length() + 5);
        out.writeByte(getType().getVal());
        out.writeInt(transactionId);
    }

    protected abstract Command readBody(ByteBuf out);

    public void readHeader(ByteBuf in) {
        this.transactionId = in.readInt();
    }

    public Command read(ByteBuf in) {
        readHeader(in);
        readBody(in);
        return this;
    }

    public abstract COMMAND_TYPE getType();

    public enum COMMAND_TYPE {
        GET((byte) 1),
        PUT((byte) 2),
        MULTIGET((byte) 3),
        EXIST((byte) 4),
        DELETE((byte) 5),


        GET_RESPONSE((byte) 20),
        PUT_RESPONSE((byte) 21),
        DELETE_RESPONSE((byte) 22),
        EXIST_RESPONSE((byte) 23),
        MULTIGET_RESPONSE((byte) 24),


        PING((byte) 60),
        PONG((byte) 61),

        MASTER_SYNC((byte) 62),
        MASTER_ACK((byte) 63),
        MASTER_NONE_SYNC((byte) 63),


        //   Slave -> MasterServer
        GET_UPDATE_SINCE((byte) 71),
        GET_UPDATE_SINCE_RESPONSE((byte) 72),

        GET_LATEST_SEQ((byte) 73),
        GET_LATEST_SEQ_RESPONSE((byte) 74),

        //  MasterServer->Salve
        SEND_UPDATE((byte) 80),
        SEND_UPDATE_RESPONSE((byte) 81),

        SEND_SEQ((byte) 82),//相互通信，发送自己的最大seq

        EXCEPTION_RESPONSE((byte) 127);
        private byte val;

        COMMAND_TYPE(byte val) {
            this.val = val;
        }

        public byte getVal() {
            return val;
        }
    }

    public abstract int length();

    public int size() {
        return length() + 9;
    }

    public static byte[] readIntHeadContent(ByteBuf buf) {
        if (buf == null || buf.readableBytes() < 4) {
            return null;
        }
        int len = buf.readInt();
        byte[] val = new byte[len];
        buf.readBytes(val);
        return val;
    }

    public static int writeIntHeadContent(byte[] content, ByteBuf buf) {
        if (buf == null || content == null || content.length == 0) {
            buf.writeInt(0);
            return 0;
        }
        buf.writeInt(content.length);
        buf.writeBytes(content);
        return content.length;
    }


    public static Command get(ByteBuf in) {
        int len = in.readInt();
        byte command = in.readByte();
        if (command == Command.COMMAND_TYPE.GET.getVal()) {
            return new GetCommand().read(in);
        } else if (command == Command.COMMAND_TYPE.DELETE.getVal()) {
            return new DeleteCommand().read(in);
        } else if (command == Command.COMMAND_TYPE.PUT.getVal()) {
            return new PutCommand().read(in);
        } else if (command == Command.COMMAND_TYPE.EXIST.getVal()) {
            return new ExistCommand().read(in);
        } else if (command == Command.COMMAND_TYPE.MULTIGET.getVal()) {
            return new MultiGetCommand().read(in);
        } else if ((command == Command.COMMAND_TYPE.GET_LATEST_SEQ.getVal())) {
            return new GetLatestSequenceNumCommand().read(in);
        } else if (command == Command.COMMAND_TYPE.PUT_RESPONSE.getVal()) {
            return new PutResponse().read(in);
        } else if (command == Command.COMMAND_TYPE.GET_RESPONSE.getVal()) {
            return new GetResponse().read(in);
        } else if (command == Command.COMMAND_TYPE.GET_LATEST_SEQ_RESPONSE.getVal()) {
            return new GetLatestSequenceNumResponse().read(in);
        } else if (command == COMMAND_TYPE.EXCEPTION_RESPONSE.getVal()) {
            return new ExceptionResponse().read(in);
        } else {
            throw new RuntimeException("Unknown command found.");
        }
    }

}
