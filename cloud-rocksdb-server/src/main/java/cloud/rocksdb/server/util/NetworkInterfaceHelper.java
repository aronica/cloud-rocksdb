package cloud.rocksdb.server.util;

import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by jie.huang on 15/8/18.
 */
public enum NetworkInterfaceHelper {
    INSTANCE;
    InetAddress local;

    NetworkInterfaceHelper() {
        load();
    }

    public String getLocalHostAddress() {
        return local.getHostAddress();
    }

    public String getLocalHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return local.getHostName();
        }
    }

    private String getProperty(String name) {
        String value = System.getProperty(name);

        if (value == null) {
            value = System.getenv(name);
        }

        return value;
    }

    private void load() {
        String ip = getProperty("host.ip");

        if (ip != null) {
            try {
                local = InetAddress.getByName(ip);
                return;
            } catch (Exception e) {
                System.err.println(e);
            }
        }

        try {
            List<NetworkInterface> nis = Collections.list(NetworkInterface.getNetworkInterfaces());
            List<InetAddress> addresses = new ArrayList<>();
            InetAddress local = null;

            try {
                for (NetworkInterface ni : nis) {
                    if (ni.isUp()) {
                        addresses.addAll(Collections.list(ni.getInetAddresses()));
                    }
                }
                local = findValidateIp(addresses);
            } catch (Exception e) {
                // ignore
            }
            this.local = local;
        } catch (SocketException ignore) {
        }
    }

    public InetAddress findValidateIp(List<InetAddress> addresses) {
        InetAddress local = null;
        for (InetAddress address : addresses) {
            if (address instanceof Inet4Address) {
                if (address.isLoopbackAddress() || address.isSiteLocalAddress()) {
                    if (local == null) {
                        local = address;
                    } else if (address.isSiteLocalAddress() && !address.isLoopbackAddress()) {
                        local = address;
                    } else if (local.isSiteLocalAddress() && address.isSiteLocalAddress()) {
                        if (local.getHostName().equals(local.getHostAddress())
                                && !address.getHostName().equals(address.getHostAddress())) {
                            local = address;
                        }
                    }
                } else {
                    if (local == null) {
                        local = address;
                    }
                }
            }
        }
        return local;
    }

}
