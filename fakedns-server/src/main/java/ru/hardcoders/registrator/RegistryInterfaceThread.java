package ru.hardcoders.registrator;

import ru.hardcoders.dns.Registry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by root on 08.06.15.
 */
public class RegistryInterfaceThread extends Thread {

    private static final Logger logger = Logger.getLogger(RegistryInterfaceThread.class.getName());

    private static final int SO_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(25L);

    private final ExecutorService executor;
    private final InetSocketAddress address;
    private final Registry registry;
    private final int timeout;

    public RegistryInterfaceThread(InetSocketAddress address, boolean daemon) {
        this(address, daemon, SO_TIMEOUT);
    }

    public RegistryInterfaceThread(InetSocketAddress address, boolean daemon, int timeout) {
        this(Executors.newFixedThreadPool(20), address, daemon, timeout);
    }

    public RegistryInterfaceThread(ExecutorService executor, InetSocketAddress address, boolean daemon, int timeout) {
        this(executor, new Registry(), address, daemon, timeout);
    }

    public RegistryInterfaceThread(ExecutorService executor, Registry registry, InetSocketAddress address, boolean daemon, int timeout) {
        this.executor = executor;
        this.registry = registry;
        this.address = address;
        this.timeout = timeout;
        setDaemon(daemon);
    }

    @Override
    public void run() {
        try {
            ServerSocket socket = new ServerSocket(address.getPort(), 50, address.getAddress());
            while (!Thread.currentThread().isInterrupted()) {
                Socket client = socket.accept();
                client.setSoTimeout(timeout);
                executor.submit(new RegistrationWorker(registry, client));
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            System.exit(-1);
        }
    }

    private static final class RegistrationWorker implements Runnable {

        private final Socket client;
        private final Registry registry;

        public RegistrationWorker(Registry registry, Socket client) {
            this.registry = registry;
            this.client = client;
        }

        @Override
        public void run() {
            try {
                InetAddress address = client.getInetAddress();
                BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream(), "ASCII"));
                String hostname = reader.readLine();
                if (hostname != null && hostname.length() > 2) {
                    registry.put(hostname, address);
                }
            } catch (IOException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            } finally {
                try {
                    client.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }

    }
}
