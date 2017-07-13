package me.ele.cloud.rocksdb.test;

import cloud.rocksdb.server.client.BinaryClient;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by fafu on 2017/7/13.
 */
public class ClientTest {
    private BinaryClient binaryClient;

    @Before
    public void init(){
        binaryClient = new BinaryClient("10.12.33.88",9000);
    }

    @Test
    public void testConnectMaster() throws Exception {
        for (int i = 0;i<1000;i++){
            binaryClient.put(new String("key"+i).getBytes("utf-8"),new String("value"+i).getBytes());
            System.out.println(new String(binaryClient.get(new String("key"+i).getBytes("utf-8"))));
            Thread.sleep(100);
        }
    }
}
