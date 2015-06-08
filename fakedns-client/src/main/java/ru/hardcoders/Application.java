package ru.hardcoders;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Application {

    private static final Logger logger = Logger.getLogger(Application.class.getName());

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("You MUST specify 3 args: destination hostname and destination port, and your 'fake' hostname.");
            System.exit(-2);
        }
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
        String fakeHostname = args[2];

        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(hostname, port));
            Writer writer = new PrintWriter(socket.getOutputStream());
            writer.write(fakeHostname + "\n");
            writer.flush();
            socket.close();
            System.out.println("OK");
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            System.exit(-1);
        }
    }
}
