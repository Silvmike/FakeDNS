package ru.hardcoders.dns;

import ru.hardcoders.dns.api.DNS;
import ru.hardcoders.dns.impl.transport.QueryMessage;

import java.io.Closeable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by silvmike on 08.06.15.
 */
public class DNSServerThread extends Thread implements Closeable {

    private static final int DNS_PORT = 53;
    private static final Logger logger = Logger.getLogger(DNSServerThread.class.getName());

    private volatile boolean closed = false;

    private final String hostname;
    private final DNS dns;
    private final int port;

    public DNSServerThread(DNS dns, String hostname) {
        this(dns, hostname, DNS_PORT);
    }

    public DNSServerThread(DNS dns, String hostname, int port) {
        this.dns = dns;
        this.hostname = hostname;
        this.port = port;
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(new InetSocketAddress(hostname, port))) {

            var buffer = new byte[512];
            var packet = new DatagramPacket(buffer, buffer.length);

            while (!closed && !Thread.currentThread().isInterrupted()) {

                packet.setData(buffer, 0, buffer.length);
                try {
                    socket.receive(packet);
                    byte[] response = dns.response(new QueryMessage(packet.getData())).toBytes();
                    packet.setData(response);
                    socket.send(packet);
                } catch (Exception e) {
                    if (e instanceof SocketException se) throw se;
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }

        } catch (SocketException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        this.closed = true;
        this.interrupt();
    }
}
