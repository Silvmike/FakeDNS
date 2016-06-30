package ru.hardcoders.dns;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by root on 08.06.15.
 */
public class Registry {

    private static final Map<String, InetAddress> registry = new ConcurrentHashMap<String, InetAddress>();

    public Registry() {}

    public void put(String hostname, InetAddress address) {
        registry.put(hostname, address);
    }

    public InetAddress resolve(String hostname) {
        return registry.get(hostname);
    }

}
