package ru.hardcoders.dns.api.registry;

import java.net.InetAddress;

/**
 * Created by silvmike on 08.06.15.
 */
public interface Registry {
    void put(String hostname, InetAddress address);
    InetAddress resolve(String hostname);
}
