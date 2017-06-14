package cloud.rocksdb.server.client;

import cloud.rocksdb.server.command.Command;
import cloud.rocksdb.server.command.Response;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by fafu on 2017/6/6.
 */
public class Connection implements AutoCloseable {

    private static final byte[][] EMPTY_ARGS = new byte[0][];

    private String host = "localhost";
    private int port = 8000;
    private Socket socket;
    private BufferedInputStream inputStream;
    private BufferedOutputStream outputStream;
    private int connectionTimeout = 3000;
    private int soTimeout = 2000;
    private boolean broken = false;

    private static final String HOST_DEFAULT = "127.0.0.1";
    private static final int PORT_DEFAULT = 8000;


    public Connection() throws IOException {
        this(HOST_DEFAULT,PORT_DEFAULT);
    }

    public Connection(final String host) {
        this(host,PORT_DEFAULT);
    }

    public Connection(final String host, final int port) {
        this.host = host;
        this.port = port;
        connect();
    }

    public Connection(final String host, final int port,int connectionTimeout) {
        this.host = host;
        this.port = port;
        this.connectionTimeout = connectionTimeout;
        connect();
    }

    public Connection(final String host, final int port,int connectionTimeout,int soTimeout) {
        this.host = host;
        this.port = port;
        this.connectionTimeout = connectionTimeout;
        this.soTimeout = soTimeout;
        connect();
    }

    public void sendCommand(Command command)  {
        try {
            Protocal.writeCommand(command, outputStream);
        }catch (IOException e){
            broken = true;
            throw new RocksdbConnectionException(e);
        }
    }

    public Response<?> readCommand()  {
        try {
            this.flush();
            return (Response<?>) Protocal.readCommand(inputStream);
        }catch (IOException e){
            broken = true;
            throw new RocksdbConnectionException(e);
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public int getSoTimeout() {
        return soTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    public void setTimeoutInfinite() {
        try {
            if (!isConnected()) {
                connect();
            }
            socket.setSoTimeout(0);
        } catch (SocketException ex) {
            broken = true;
            throw new RocksdbConnectionException(ex);
        }
    }

    public void rollbackTimeout() {
        try {
            socket.setSoTimeout(soTimeout);
        } catch (SocketException ex) {
            broken = true;
            throw new RocksdbConnectionException(ex);
        }
    }

    public void connect() {
        if (!isConnected()) {
            try {
                socket = new Socket();
                // ->@wjw_add
                socket.setReuseAddress(true);
                socket.setKeepAlive(true); // Will monitor the TCP connection is
                // valid
                socket.setTcpNoDelay(true); // Socket buffer Whetherclosed, to
                // ensure timely delivery of data
                socket.setSoLinger(true, 0); // Control calls close () method,
                // the underlying socket is closed
                // immediately
                // <-@wjw_add

                socket.connect(new InetSocketAddress(host, port), connectionTimeout);
                socket.setSoTimeout(soTimeout);
                outputStream = new BufferedOutputStream(socket.getOutputStream());
                inputStream = new BufferedInputStream(socket.getInputStream());
            } catch (IOException ex) {
                broken = true;
                throw new RocksdbConnectionException(ex);
            }
        }
    }


    public boolean isConnected() {
        return socket != null && socket.isBound() && !socket.isClosed() && socket.isConnected()
                && !socket.isInputShutdown() && !socket.isOutputShutdown();
    }

    @Override
    public void close() throws Exception {
        if (isConnected()) {
            try {
                outputStream.flush();
                socket.close();
            } catch (IOException ex) {
                broken = true;
                throw new RocksdbConnectionException(ex);
            } finally {
                IOUtils.closeQuietly(socket);
            }
        }
    }

    private void flush(){
        try {
            this.outputStream.flush();
        } catch (IOException var2) {
            this.broken = true;
            throw new RocksdbConnectionException(var2);
        }
    }

    public boolean isBroken() {
        return broken;
    }
}
