package cloud.rocksdb.server.client.command;

/**
 * Created by fafu on 2017/6/9.
 */
public class ExceptionResponse extends Response<byte[]> {

    public ExceptionResponse(){
        super();
    }
    public ExceptionResponse(byte code, byte[] exe) {
        super(code,exe);
    }

    @Override
    public COMMAND_TYPE getType() {
        return COMMAND_TYPE.EXCEPTION_RESPONSE;
    }
}
