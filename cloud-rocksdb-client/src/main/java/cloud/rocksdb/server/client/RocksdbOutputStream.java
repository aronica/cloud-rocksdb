package cloud.rocksdb.server.client;

import java.io.FilterOutputStream;
import java.io.OutputStream;

/**
 * Created by fafu on 2017/6/7.
 */
public class RocksdbOutputStream extends FilterOutputStream {
    /**
     * Creates an output stream filter built on top of the specified
     * underlying output stream.
     *
     * @param out the underlying output stream to be assigned to
     *            the field <tt>this.out</tt> for later use, or
     *            <code>null</code> if this instance is to be
     *            created without an underlying stream.
     */
    public RocksdbOutputStream(OutputStream out) {
        super(out);
    }


}
