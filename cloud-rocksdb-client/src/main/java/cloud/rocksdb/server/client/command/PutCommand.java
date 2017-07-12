package cloud.rocksdb.server.client.command;

import lombok.NoArgsConstructor;

/**
 * Created by fafu on 2017/5/30.
 */
@NoArgsConstructor
public class PutCommand extends MultiFieldCommand {


    public PutCommand(byte[] key,byte[]content) {
        super(key,content);
    }

    public COMMAND_TYPE getType() {
        return COMMAND_TYPE.PUT;
    }

    public byte[] getKey() {
        return content[0];
    }


    public byte[] getValue() {
        return content[1];
    }

}
