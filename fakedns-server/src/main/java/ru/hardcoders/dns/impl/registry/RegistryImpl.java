package ru.hardcoders.dns.impl.registry;

import ru.hardcoders.dns.api.registry.Registry;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by silvmike on 08.06.15.
 */
public class RegistryImpl implements Registry {

    private final Map<String, InetAddress> registry = new ConcurrentHashMap<String, InetAddress>();

    public RegistryImpl() {}

    @Override
    public void put(String hostname, InetAddress address) {
        registry.put(hostname, address);
    }

    @Override
    public InetAddress resolve(String hostname) {
        return registry.get(hostname);
    }

}
