package cloud.rocksdb.client;

import cloud.rocksdb.ObjectUtil;

/**
 * Created by fafu on 2017/6/1.
 */
public class ClientMain {
    public static void main(String[] args) throws Exception {
        ConnectionPool pool = new ConnectionPool("localhost",8888,10);
        BinaryClient client = new BinaryClient(pool);
        client.put(ObjectUtil.convert("key"),ObjectUtil.convert("value"));
        String value = ObjectUtil.convert(client.get(ObjectUtil.convert("key")));
        System.out.println(value);
    }
}
