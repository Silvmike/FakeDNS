package ru.hardcoders;

import ru.hardcoders.dns.DNSServerThread;
import ru.hardcoders.registrator.RegistryInterfaceThread;

import java.net.InetSocketAddress;
import java.util.Arrays;

public class Application {

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
        Args arguments = new Args(args);
        DNSServerThread serverThread = new DNSServerThread(arguments.hostname());
        RegistryInterfaceThread registryInterfaceThread = new RegistryInterfaceThread(arguments.bindAddress(), true);
        new Application(serverThread, registryInterfaceThread).run();
    }

    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                registryInterfaceThread.close();
                serverThread.close();
            }
        }));
        serverThread.start();
        registryInterfaceThread.start();
    }

    private static final class Args {

        private final String[] args;

        public Args(String[] args) {
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
