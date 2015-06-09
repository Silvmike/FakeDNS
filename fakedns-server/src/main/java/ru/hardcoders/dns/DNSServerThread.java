package ru.hardcoders.dns;

import java.io.IOException;
import java.net.*;
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

    public DNSServerThread(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public void run() {
        DatagramPacket packet = new DatagramPacket(new byte[512], 512);
        try (DatagramSocket socket = new DatagramSocket(new InetSocketAddress(hostname, DNS_PORT))) {

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    socket.receive(packet);
                    DNSMessage message = new DNSMessage(packet.getData());
                    InetAddress address = Registry.resolve(message.getQuestionName());
                    DNSAnswerHelper answerHelper = new DNSAnswerHelper(message);
                    if (address != null) {
                        answerHelper.setAddress(address);
                        answerHelper.setTimeToLiveSeconds(TTL_SECONDS);
                        answerHelper.setErrorCode(DNSHeaderHelper.ErrorCode.NO_ERROR);
                    } else {
                        answerHelper.setErrorCode(DNSHeaderHelper.ErrorCode.DOMAIN_NOT_FOUND);
                    }
                    byte[] answer = answerHelper.build();
                    packet.setData(answer);
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
