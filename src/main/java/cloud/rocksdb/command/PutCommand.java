package cloud.rocksdb.command;

import lombok.NoArgsConstructor;

/**
 * Created by fafu on 2017/5/30.
 */
@NoArgsConstructor
public class PutCommand extends MultiFieldCommand {

    private byte[] key;
    private byte[] value;

    public PutCommand(byte[] key,byte[]content) {
        super(key,content);
        this.key = key;
        this.value = content;
    }

    public COMMAND_TYPE getType() {
        return COMMAND_TYPE.PUT;
    }

    public byte[] getKey() {
        return key;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }
}
