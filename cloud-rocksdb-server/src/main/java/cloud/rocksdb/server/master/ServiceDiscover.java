package cloud.rocksdb.server.master;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.curator.x.discovery.strategies.RandomStrategy;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by fafu on 2017/7/12.
 */
public class ServiceDiscover {

    private ServiceDiscovery<Container> serviceDiscovery;
    private final ConcurrentHashMap<String, ServiceProvider<Container>> serviceProviderMap = new ConcurrentHashMap<>();

    public ServiceDiscover(CuratorFramework client , String basePath){
        serviceDiscovery = ServiceDiscoveryBuilder.builder(Container.class)
                .client(client)
                .basePath(basePath)
                .serializer(new JsonInstanceSerializer<>(Container.class))
                .build();
    }

    /**
     * Note: When using Curator 2.x (Zookeeper 3.4.x) it's essential that service provider objects are cached by your application and reused.
     * Since the internal NamespaceWatcher objects added by the service provider cannot be removed in Zookeeper 3.4.x,
     * creating a fresh service provider for each call to the same service will eventually exhaust the memory of the JVM.
     */
    public ServiceInstance<Container> getServiceProvider(String serviceName) throws Exception {
        ServiceProvider<Container> provider = getContainerServiceProvider(serviceName);
        return provider.getInstance();
    }

    private ServiceProvider<Container> getContainerServiceProvider(String serviceName) throws Exception {
        ServiceProvider<Container> provider = serviceProviderMap.get(serviceName);
        if (provider == null) {
            provider = serviceDiscovery.serviceProviderBuilder().
                    serviceName(serviceName).
                    providerStrategy(new RandomStrategy<Container>())
                    .build();

            ServiceProvider<Container> oldProvider = serviceProviderMap.putIfAbsent(serviceName, provider);
            if (oldProvider != null) {
                provider = oldProvider;
            }else {
                provider.start();
            }
        }
        return provider;
    }

    public Set<ServiceInstance<Container>> getAll(String serviceName) throws Exception{
        ServiceProvider<Container> provider = getContainerServiceProvider(serviceName);
        return new HashSet<>(provider.getAllInstances());
    }

    public Map<String,ServiceInstance<Container>> getAllMap(String serviceName) throws Exception{
        ServiceProvider<Container> provider = getContainerServiceProvider(serviceName);
        return provider.getAllInstances().stream().collect(Collectors.toMap(i->i.getId(),i->i));
    }

    public List<ServiceInstance<Container>> getAllInstance(String serviceName){
        try{
            List<ServiceInstance<Container>> instances = new ArrayList<>(getAll(serviceName));
            instances.sort((a,b)->a.getId().compareTo(b.getId()));
            return instances;
        }catch (Exception e){
            e.printStackTrace();
            return Collections.EMPTY_LIST;
        }
    }

    public void start() throws Exception {
        serviceDiscovery.start();
    }

    public void close() throws IOException {

        for (Map.Entry<String, ServiceProvider<Container>> me : serviceProviderMap.entrySet()){
            try{
                me.getValue().close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        serviceDiscovery.close();
    }
}
