package ru.hardcoders.dns;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by silvmike on 08.06.15.
 */
public class Registry {

    private final Map<String, InetAddress> registry = new ConcurrentHashMap<String, InetAddress>();

    public Registry() {}

    public void put(String hostname, InetAddress address) {
        registry.put(hostname, address);
    }

    public InetAddress resolve(String hostname) {
        return registry.get(hostname);
    }

}
