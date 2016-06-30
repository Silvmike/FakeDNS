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
        try {
            RegisterRequest request = new RegisterRequest(new Args(args));
            request.send();
            System.out.println("OK");
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            System.exit(-1);
        }
    }

    private static final class RegisterRequest {

        private final String registrant;
        private final InetSocketAddress address;

        public RegisterRequest(Args args) {
            this.registrant = args.registrant();
            this.address = args.address();
        }

        public RegisterRequest(String registrant, InetSocketAddress address) {
            this.registrant = registrant;
            this.address = address;
        }

        public void send() throws IOException {
            Socket socket = new Socket();
            socket.connect(address);
            Writer writer = new PrintWriter(socket.getOutputStream());
            writer.write(registrant);
            writer.write("\n");
            writer.flush();
            socket.close();
        }
    }

    private static final class Args {

        private final String[] args;

        public Args(String[] args) {
            this.args = new String[args.length];
            System.arraycopy(args, 0, this.args, 0, this.args.length);
        }

        public String registrant() {
            return args[2];
        }

        public InetSocketAddress address() {
            return new InetSocketAddress(hostname(), port());
        }

        private String hostname() {
            return args[0];
        }

        private int port() {
            return Integer.parseInt(args[1]);
        }

    }
}
