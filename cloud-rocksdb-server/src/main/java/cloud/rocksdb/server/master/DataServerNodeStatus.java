package cloud.rocksdb.server.master;

/**
 * Created by fafu on 2017/7/18.
 */
public enum DataServerNodeStatus {
    INIT(0),SERVING(1),DOWNING(2),DOWN(3);

    private int value;
    private DataServerNodeStatus(int value){
        this.value = value;
    }

    public int getValue(){
        return this.value;
    }
}
