package cloud.rocksdb.server.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class ZKUtils {
    private final static Logger LOGGER = LoggerFactory.getLogger(ZKUtils.class);

    public static void createEphemeralPath(CuratorFramework curator, String path, byte[] data) throws Exception {
        curator.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path, data);
    }

    public static void createPersistentPath(CuratorFramework curator, String path, byte[] data) throws Exception {
        curator.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, data);
    }

    public static void createPersistentPath(CuratorFramework curator, String path) throws Exception {
        curator.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
    }

    public static void createPersistentPathIfNotExist(CuratorFramework curator, String path) {
        try {
            if (!exists(curator, path)) {
                createPersistentPath(curator, path);
            }
        } catch (Exception e) {
            LOGGER.error("create path error {} .", path, e);
        }
    }

    public static void createPersistentPathIfNotExist(CuratorFramework curator, String path,byte[] data) {
        try {
            if (!exists(curator, path)) {
                createPersistentPath(curator, path,data);
            }
        } catch (Exception e) {
            LOGGER.error("create path error {} .", path, e);
        }
    }

    public static void setDataIfExists(CuratorFramework curator, String path, byte[] data) throws Exception {
        if(exists(curator, path)){
            curator.setData().forPath(path, data);
        }
    }

    public static boolean exists(CuratorFramework curator, String path) throws Exception {
        Stat stat = curator.checkExists().forPath(path);
        return stat != null;
    }

    public static void setPersistentData(CuratorFramework curator, String path, byte[] data) throws Exception {
        if (exists(curator, path)) {
            curator.setData().forPath(path, data);
        } else {
            curator.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, data);
        }
    }

    public static void deletePath(CuratorFramework curator, String path) throws Exception {
        if (exists(curator, path)) {
            curator.delete().deletingChildrenIfNeeded().forPath(path);
        }
    }

    public static byte[] getInformation(CuratorFramework curator, String path) throws Exception {
        if (exists(curator, path)) {
            byte[] data = curator.getData().forPath(path);
            if (data != null && data.length > 0) {
                return data;
            }
        }
        return null;
    }

    public static List<String> getChildren(CuratorFramework curator, String path) throws Exception {
        if (!exists(curator, path)) {
            return null;
        }
        return curator.getChildren().forPath(path);
    }

    public static List<String> getChildrenPaths(CuratorFramework curator, String path) throws Exception {
        List<String> childrenList = ZKUtils.getChildren(curator, path);
        if (childrenList == null || childrenList.size() == 0) {
            return childrenList;
        }
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        List<String> pathList = new ArrayList<>();
        for (String childrenName : childrenList) {
            pathList.add(path + childrenName);
        }
        return pathList;
    }

}
