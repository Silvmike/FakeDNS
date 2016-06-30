package ru.hardcoders.dns.transport;

import java.io.UnsupportedEncodingException;

/**
 * Created by silvmike on 30.06.16.
 * See <a href="https://justanapplication.wordpress.com/category/dns/dns-messages/dns-message-format/dns-message-header-format/">DNS Message Header Format</a>.
 */
public class QueryMessage {

    private static final int DNS_HEADER_LENGTH = 12;

    private final byte[] message;

    public QueryMessage(byte[] message) {
        byte[] result = new byte[message.length];
        System.arraycopy(message, 0, result, 0, result.length);
        this.message = result;
    }

    public Header header() {
        byte[] header = new byte[DNS_HEADER_LENGTH];
        System.arraycopy(this.message, 0, header, 0, header.length);
        return new Header.BytesHeader(header);
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
        byte[] result = new byte[this.message.length];
        System.arraycopy(this.message, 0, result, 0, result.length);
        return result;
    }
}
