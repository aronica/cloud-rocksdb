package cloud.rocksdb.server.client;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by fafu on 2017/6/9.
 */

public class IOUtils {
    private IOUtils() {
    }

    public static void closeQuietly(Socket sock) {
        // It's same thing as Apache Commons - IOUtils.closeQuietly()
        if (sock != null) {
            try {
                sock.close();
            } catch (IOException e) {
                // ignored
            }
        }
    }
}

