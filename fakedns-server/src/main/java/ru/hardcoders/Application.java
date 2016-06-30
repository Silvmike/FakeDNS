package ru.hardcoders;

import ru.hardcoders.dns.DNSServerThread;
import ru.hardcoders.registrator.RegistratorThread;

import java.net.InetSocketAddress;

public class Application {

    private final DNSServerThread serverThread;
    private final RegistratorThread registratorThread;

    public Application(DNSServerThread serverThread, RegistratorThread registratorThread) {
        this.serverThread = serverThread;
        this.registratorThread = registratorThread;
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("You should specify 2 args: hostname to bind and port to listen.");
            System.exit(-2);
        }
        Args arguments = new Args(args);
        DNSServerThread serverThread = new DNSServerThread(arguments.hostname());
        RegistratorThread registratorThread = new RegistratorThread(
                                                    new InetSocketAddress(arguments.hostname(),
                                                                          arguments.port()),
                                                    true
                                                  );
        new Application(serverThread, registratorThread).run();
    }

    public void run() {
        serverThread.start();
        registratorThread.start();
    }

    private static final class Args {

        private final String[] args;

        public Args(String[] args) {
            this.args = new String[args.length];
            System.arraycopy(args, 0, this.args, 0, this.args.length);
        }

        public String hostname() {
            return args[0];
        }

        public int port() {
            return Integer.parseInt(args[1]);
        }
    }
}
