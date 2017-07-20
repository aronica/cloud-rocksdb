package cloud.rocksdb.server.master;


import lombok.Data;

/**
 * @author yufu.deng
 *         Date: 16/8/4
 *         Time: 11:53
 */

@Data
public class Container  {
    public String host;
    public int port;
    public String shardId;
    public long startup;
    public long lastModifyTime;
    public int status = 1;

    public Container() {
    }

    public Container(String shardId,String host, int port) {
        this.host = host;
        this.port = port;
        this.startup = System.currentTimeMillis();
        this.lastModifyTime = System.currentTimeMillis();
        this.shardId = shardId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Container)) return false;

        Container container = (Container) o;

        if (port != container.port) return false;
        return !(host != null ? !host.equals(container.host) : container.host != null);

    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }
}
