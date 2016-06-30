package ru.hardcoders;

import ru.hardcoders.dns.DNSServerThread;
import ru.hardcoders.registrator.RegistratorThread;

import java.net.InetSocketAddress;

public class Application {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("You should specify 2 args: hostname to bind and port to listen.");
            System.exit(-2);
        }
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
        new DNSServerThread(hostname).start();
        new RegistratorThread(new InetSocketAddress(hostname, port)).start();
    }
}
