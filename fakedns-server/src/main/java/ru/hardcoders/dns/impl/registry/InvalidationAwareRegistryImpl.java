package ru.hardcoders.dns.impl.registry;

import ru.hardcoders.dns.api.registry.Registry;

import java.net.InetAddress;

public class InvalidationAwareRegistryImpl implements Registry {

    private final Registry registry;
    private final InvalidationListener listener;

    public InvalidationAwareRegistryImpl(Registry registry, InvalidationListener listener) {
        this.registry = registry;
        this.listener = listener;
    }

    @Override
    public void put(String hostname, InetAddress address) {
        registry.put(hostname, address);
        listener.onChange(hostname);
    }

    @Override
    public InetAddress resolve(String hostname) {
        return registry.resolve(hostname);
    }

    public interface InvalidationListener {
        void onChange(String hostname);
    }
}
