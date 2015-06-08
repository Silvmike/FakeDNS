package ru.hardcoders.dns;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Created by root on 08.06.15
 * See <a href="https://justanapplication.wordpress.com/category/dns/dns-messages/dns-message-format/dns-message-header-format/">DNS Message Header Format</a>.
 */
public class DNSMessage {

    public static final int DNS_HEADER_LENGTH = 12 /* byte */;

    private final byte[] message;

    /**
     * 6 x 16bit fields:
     * - TransactionID
     * - Flags
     * - Questions count
     * - Answers count
     * - Authority  count
     * - Additional count
     */
    private final byte[] header = new byte[DNS_HEADER_LENGTH];

    private final int endOfQuestionPosition;
    private final String questionName; // we'll work only with first question

    public DNSMessage(byte[] message) {
        this.message = message;
        System.arraycopy(message, 0, header, 0, DNS_HEADER_LENGTH);
        this.endOfQuestionPosition = findEndOfQuestion(message);
        this.questionName = extractQuestion(message, DNS_HEADER_LENGTH, endOfQuestionPosition);

    }

    private int findEndOfQuestion(byte[] message) {
        int pos = DNS_HEADER_LENGTH;
        for ( ; pos<message.length; pos++) {
            if (message[pos] == (byte) 0) {
                break;
            }
        }
        return pos;
    }

    private String extractQuestion(byte[] message, int start, int end) {
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

    public byte[] getMessage() {
        return Arrays.copyOf(message, message.length);
    }

    public byte[] getHeader() {
        return Arrays.copyOf(header, header.length);
    }

    public String getQuestionName() {
        return questionName;
    }

    public int getEndOfQuestionPosition() {
        return endOfQuestionPosition;
    }
}
