package ru.hardcoders.registrator;

import ru.hardcoders.dns.api.registry.Registry;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by root on 08.06.15.
 */
public class RegistryInterfaceThread extends Thread implements Closeable {

    private static final Logger logger = Logger.getLogger(RegistryInterfaceThread.class.getName());

    private volatile boolean closed = false;

    private final InetSocketAddress address;
    private final Registry registry;

    public RegistryInterfaceThread(Registry registry, InetSocketAddress address, boolean daemon) {
        this.registry = registry;
        this.address = address;
        setDaemon(daemon);
    }

    @Override
    public void run() {
        try (ServerSocketChannel channel = ServerSocketChannel.open()) {

            channel.configureBlocking(false);
            channel.bind(address);

            Selector selector = Selector.open();
            channel.register(selector, SelectionKey.OP_ACCEPT);

            logger.log(Level.INFO, "Ready to accept new registrations!");

            while (!closed && !Thread.currentThread().isInterrupted()) {
                if (selector.select() > 0) {
                    for (Iterator<SelectionKey> iterator = selector.selectedKeys().iterator(); iterator.hasNext(); ) {
                        SelectionKey selectionKey = iterator.next();
                        iterator.remove();
                        if (selectionKey.isAcceptable()) {
                            acceptClient(selectionKey);
                        } else {
                            ((Handler) selectionKey.attachment()).handle(selectionKey);
                        }
                    }
                }
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            if (!closed) {
                System.exit(-1);
            }
        }
    }

    private void acceptClient(SelectionKey selectionKey) {
        try {
            SocketChannel socketChannel = ((ServerSocketChannel) selectionKey.channel()).accept();
            socketChannel.configureBlocking(false);
            SelectionKey clientSelectionKey = socketChannel.register(selectionKey.selector(), SelectionKey.OP_READ);
            clientSelectionKey.attach(new Handler(socketChannel, registry));
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        this.closed = true;
        this.interrupt();
    }

    private static final class Handler {

        private static final Charset ASCII = Charset.forName("ASCII");
        private static final int FQDN_MAX_LENGTH = 255;

        private final ByteBuffer buffer = ByteBuffer.allocate(FQDN_MAX_LENGTH);
        private final SocketChannel channel;
        private final Registry registry;

        public Handler(SocketChannel channel, Registry registry) {
            this.channel = channel;
            this.registry = registry;
        }

        public void handle(SelectionKey key) {
            try {
                if (key.isReadable()) {
                    logger.fine("Start reading");
                    if (channel.read(buffer) > 0) {
                        buffer.flip();
                        if (buffer.get(buffer.limit() - 1) == '\n') {

                            String address = ASCII.decode(buffer).toString();
                            address = address.substring(0, address.indexOf('\n'));
                            if (!address.isEmpty() && address.charAt(address.length() - 1) == '\r') {
                                address = address.substring(0, address.length() - 1);
                            }
                            InetAddress inetAddress = ((InetSocketAddress) channel.getRemoteAddress()).getAddress();
                            registry.put(address, inetAddress);
                            logger.info("Registered " + inetAddress + " as " + address);
                            key.interestOps(0);
                            key.cancel();
                            channel.close();

                        }
                        buffer.compact();
                    }
                }
            } catch (IOException ioException) {
                try {
                    channel.close();
                } catch (IOException onCloseException) {
                    ioException.addSuppressed(onCloseException);
                }
                logger.log(Level.SEVERE, ioException.getMessage(), ioException);
            }
        }

    }

}
