package ru.hardcoders.dns;

import ru.hardcoders.dns.transport.CompressedResponse;
import ru.hardcoders.dns.transport.QueryMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by root on 08.06.15.
 */
public class DNSServerThread extends Thread {

    private static final int DNS_PORT = 53;
    private static final int TTL_SECONDS = 5;
    private static final Logger logger = Logger.getLogger(DNSServerThread.class.getName());

    private final String hostname;
    private final DNS dns = new DNS(new CompressedResponse.TimeToLive(TTL_SECONDS));

    public DNSServerThread(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(new InetSocketAddress(hostname, DNS_PORT))) {

            while (!Thread.currentThread().isInterrupted()) {
                DatagramPacket packet = new DatagramPacket(new byte[512], 512);
                try {
                    socket.receive(packet);
                    byte[] response = dns.response(new QueryMessage(packet.getData())).toBytes();
                    packet.setData(response);
                    socket.send(packet);
                } catch (IOException e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }

        } catch (SocketException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

}
