package ru.hardcoders;

import ru.hardcoders.dns.DNSServerThread;
import ru.hardcoders.dns.impl.CachingDNS;
import ru.hardcoders.dns.impl.DNSImpl;
import ru.hardcoders.dns.impl.registry.InvalidationAwareRegistryImpl;
import ru.hardcoders.dns.impl.registry.RegistryImpl;
import ru.hardcoders.registrator.RegistryInterfaceThread;

import java.net.InetSocketAddress;
import java.util.Arrays;

public class Application {

    private static final int TTL_SECONDS = 5;

    private final DNSServerThread serverThread;
    private final RegistryInterfaceThread registryInterfaceThread;

    public Application(DNSServerThread serverThread, RegistryInterfaceThread registryInterfaceThread) {
        this.serverThread = serverThread;
        this.registryInterfaceThread = registryInterfaceThread;
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("You should specify 2 args: hostname to bind and port to listen.");
            System.exit(-2);
        }

        var arguments = new Args(args);

        var registry = new RegistryImpl();
        var cachingDns = new CachingDNS(
            new DNSImpl(registry, TTL_SECONDS)
        );
        var dnsThread = new DNSServerThread(cachingDns, arguments.hostname());

        var cachingRegistry = new InvalidationAwareRegistryImpl(registry, cachingDns);
        var registryThread = new RegistryInterfaceThread(cachingRegistry, arguments.bindAddress(), true);

        new Application(dnsThread, registryThread).run();
    }

    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            registryInterfaceThread.close();
            serverThread.close();
        }));
        serverThread.start();
        registryInterfaceThread.start();
    }

    private record Args(String[] args) {

        private Args(String[] args) {
            this.args = Arrays.copyOf(args, args.length);
        }

        public InetSocketAddress bindAddress() {
            return new InetSocketAddress(hostname(), port());
        }

        public String hostname() {
            return args[0];
        }

        private int port() {
            return Integer.parseInt(args[1]);
        }
    }
}
