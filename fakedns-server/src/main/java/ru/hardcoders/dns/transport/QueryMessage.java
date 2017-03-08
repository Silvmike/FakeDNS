package ru.hardcoders.dns.transport;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Created by silvmike on 30.06.16.
 * See <a href="https://justanapplication.wordpress.com/category/dns/dns-messages/dns-message-format/dns-message-header-format/">DNS Message Header Format</a>.
 */
public class QueryMessage {

    private static final int DNS_HEADER_LENGTH = 12;

    private final byte[] message;

    public QueryMessage(byte[] message) {
        this.message = Arrays.copyOf(message, message.length);
    }

    public Header header() {
        return new Header.BytesHeader(Arrays.copyOf(this.message, DNS_HEADER_LENGTH));
    }

    public String question() {
        return extractQuestion(DNS_HEADER_LENGTH, endOfQuestion());
    }

    private int endOfQuestion() {
        int pos = DNS_HEADER_LENGTH;
        for (; pos < message.length; pos++) {
            if (message[pos] == (byte) 0) {
                break;
            }
        }
        return pos;
    }

    private String extractQuestion(int start, int end) {
        StringBuilder buffer = new StringBuilder(end - start);
        try {
            for (int i = start; i < (end - 1); ) {
                char count = (char) message[i];
                byte[] string = new byte[count];
                for (int j = 0; j < count; j++) {
                    string[j] = message[i + j + 1];
                }
                buffer.append(new String(string, "ASCII")).append('.');
                i += (count + 1);
            }
            buffer.setLength(buffer.length() - 1);
        } catch (UnsupportedEncodingException e) {
            // just ignore
        }
        return buffer.toString();
    }

    public byte[] toBytes() {
        return Arrays.copyOf(this.message, this.message.length);
    }

}
