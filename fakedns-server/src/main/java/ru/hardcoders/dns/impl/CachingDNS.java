package ru.hardcoders.dns.impl;

import ru.hardcoders.dns.api.DNS;
import ru.hardcoders.dns.impl.registry.InvalidationAwareRegistryImpl;
import ru.hardcoders.dns.impl.transport.CompressedResponse;
import ru.hardcoders.dns.impl.transport.QueryMessage;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CachingDNS implements DNS, InvalidationAwareRegistryImpl.InvalidationListener {

    private final DNS dns;
    private final ConcurrentMap<String, CompressedResponse> cache = new ConcurrentHashMap<>();

    public CachingDNS(DNS dns) {
        this.dns = dns;
    }

    @Override
    public CompressedResponse response(QueryMessage message) {
        return cache.computeIfAbsent(message.question(), q -> dns.response(message));
    }

    @Override
    public void onChange(String hostname) {
        cache.remove(hostname);
    }
}
