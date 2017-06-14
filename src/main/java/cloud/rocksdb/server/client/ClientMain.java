package cloud.rocksdb.server.client;

/**
 * Created by fafu on 2017/6/1.
 */
public class ClientMain {
    public static void main(String[] args) throws Exception {
        ConnectionPool pool = new ConnectionPool("localhost",8888,10,200);
        BinaryClient client = new BinaryClient(pool);

    }
}
