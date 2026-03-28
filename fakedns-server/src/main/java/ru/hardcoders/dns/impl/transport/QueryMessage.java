package ru.hardcoders.dns.impl.transport;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Created by silvmike on 30.06.16.
 * See <a href="https://justanapplication.wordpress.com/category/dns/dns-messages/dns-message-format/dns-message-header-format/">DNS Message Header Format</a>.
 */
public record QueryMessage(byte[] message) {

    private static final int DNS_HEADER_LENGTH = 12;

    public QueryMessage(byte[] message) {
        this.message = Arrays.copyOf(message, message.length);
    }

    public Header header() {
        return Header.bytesHeader(Arrays.copyOf(this.message, DNS_HEADER_LENGTH));
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
        for (int i = start; i < (end - 1); ) {
            int count = message[i] & 0xff;
            assertLabelTooLong(count);
            byte[] string = new byte[count];
            for (char j = 0; j < count; j++) {
                assertTruncatedLabel(i, count);
                string[j] = message[i + j + 1];
            }
            buffer.append(new String(string, StandardCharsets.US_ASCII)).append('.');
            i += (count + 1);
        }
        if (end != start) buffer.setLength(buffer.length() - 1);
        return buffer.toString();
    }

    private void assertTruncatedLabel(int i, int count) {
        if (i + count + 1 > message.length) {
            throw new IllegalArgumentException("Truncated label");
        }
    }

    private static void assertLabelTooLong(int count) {
        if (count > 63) {
            throw new IllegalArgumentException("Label too long");
        }
    }

    public byte[] toBytes() {
        return Arrays.copyOf(this.message, this.message.length);
    }

}
