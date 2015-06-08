package ru.hardcoders;

import ru.hardcoders.dns.DNSServerThread;
import ru.hardcoders.registrator.RegistratorThread;

import java.net.InetSocketAddress;
import java.util.logging.Logger;

/**
 * Hello world!
 */
public class Application {

    private static final String LOCALHOST = "localhost";

    private static final Logger logger = Logger.getLogger(Application.class.getName());

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
