package cloud.rocksdb.server.client.command;

/**
 * Created by fafu on 2017/5/30.
 **/
public class MultiGetCommand extends MultiFieldCommand{

    public byte[][] getKeys(){
        return content;
    }
    public MultiGetCommand(byte[]...keys){
        super(keys);
    }

    public COMMAND_TYPE getType() {
        return COMMAND_TYPE.MULTIGET;
    }
}
