package ru.hardcoders.dns.impl.transport;

import java.util.Arrays;

/**
 * Created by silvmike on 29.06.16.
 */
public record Header(char[] header) {

    private static final int BITS_PER_BYTE = 8;
    private static final int BYTES_PER_CHAR = 2;
    private static final int BYTE_MASK = 0xff;
    private static final int HEADER_LENGTH = 6;

    public Header(Header header) {
        this(header.toChar());
    }

    public Header withId(Id id) {
        char[] header = this.toChar();
        header[0] = id.id();
        return new Header(header);
    }

    public Header withFlagsAndCodes(FlagsAndCodes flagsAndCodes) {
        char[] header = this.toChar();
        header[1] = flagsAndCodes.toChar();
        return new Header(header);
    }

    public FlagsAndCodes flagsAndCodes() {
        return new FlagsAndCodes(header[1]);
    }

    public Header withQuestionCount(Count count) {
        char[] header = this.toChar();
        header[2] = count.toChar();
        return new Header(header);
    }

    public Header withAnswerRecordCount(Count count) {
        char[] header = this.toChar();
        header[3] = count.toChar();
        return new Header(header);
    }

    public Header withAuthorityRecordCount(Count count) {
        char[] header = this.toChar();
        header[4] = count.toChar();
        return new Header(header);
    }

    public Header withAdditionalRecordCount(Count count) {
        char[] header = this.toChar();
        header[5] = count.toChar();
        return new Header(header);
    }

    public char[] toChar() {
        return Arrays.copyOf(header, header.length);
    }

    public byte[] toBytes() {
        byte[] result = new byte[BYTES_PER_CHAR * header.length];
        for (int i = 0; i < header.length; i++) {
            result[BYTES_PER_CHAR * i] = (byte) ((header[i] >> BITS_PER_BYTE) & BYTE_MASK);
            result[BYTES_PER_CHAR * i + 1] = (byte) (header[i] & BYTE_MASK);
        }
        return result;
    }

    public record Count(char count) {

        public Count(int count) {
            this((char) count);
        }

        public char toChar() {
            return count;
        }

    }

    public record Id(char id) {}

    public record FlagsAndCodes(char flagsAndCodes) {

        public FlagsAndCodes withResponse() {
            return new FlagsAndCodes((char) (flagsAndCodes | (1 << 15)));
        }

        public FlagsAndCodes withOpCode(int opCode) {
            int mask = 0x0f << 11;
            return new FlagsAndCodes((char) ((flagsAndCodes | mask) ^ mask | ((opCode & 0x0f) << 11)));
        }

        public FlagsAndCodes withAuthoritative() {
            return new FlagsAndCodes((char) (flagsAndCodes | (1 << 10)));
        }

        public FlagsAndCodes withTruncated() {
            return new FlagsAndCodes((char) (flagsAndCodes | (1 << 9)));
        }

        public FlagsAndCodes withRecursionDesired() {
            return new FlagsAndCodes((char) (flagsAndCodes | (1 << 8)));
        }

        public FlagsAndCodes withRecursionAvailable() {
            return new FlagsAndCodes((char) (flagsAndCodes | (1 << 7)));
        }

        public FlagsAndCodes withoutRecursionAvailable() {
            return new FlagsAndCodes((char) ((flagsAndCodes | (1 << 7)) ^ (1 << 7)));
        }

        public FlagsAndCodes withResponseCode(ResponseCode responseCode) {
            int mask = 0x0f;
            return new FlagsAndCodes((char) ((flagsAndCodes | mask) ^ mask | responseCode.responseCode));
        }

        public char toChar() {
            return this.flagsAndCodes;
        }

    }

    public record ResponseCode(byte responseCode) {}

    public static Header bytesHeader(byte[] bytes) {
        char[] result = new char[HEADER_LENGTH];
        for (int i = 0; i < result.length; i++) {
            result[i] = (char) (((bytes[BYTES_PER_CHAR * i] & BYTE_MASK) << 8)
                               | (bytes[BYTES_PER_CHAR * i + 1] & BYTE_MASK));
        }
        return new Header(result);
    }

}
