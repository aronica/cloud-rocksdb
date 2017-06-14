package cloud.rocksdb.server.client;

import io.netty.buffer.ByteBuf;

import java.io.FilterInputStream;
import java.io.InputStream;

/**
 * Created by fafu on 2017/6/6.
 */
public class RocksdbInputStream extends FilterInputStream{
    private ByteBuf buf;
    private InputStream in;


    /**
     * Creates a <code>FilterInputStream</code>
     * by assigning the  argument <code>in</code>
     * to the field <code>this.in</code> so as
     * to remember it for later use.
     *
     * @param in the underlying input stream, or <code>null</code> if
     *           this instance is to be created without an underlying stream.
     */
    protected RocksdbInputStream(InputStream in) {
        super(in);
    }
}
