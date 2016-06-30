package ru.hardcoders.dns.transport;

/**
 * Created by silvmike on 29.06.16.
 */
public class Header {

    private final char header[];

    public Header(Header header) {
        this(header.toChar());
    }

    private Header(char[] header) {
        this.header = header;
    }

    public Header withId(Id id) {
        char[] header = this.toChar();
        header[0] = id.toChar();
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
        char[] result = new char[header.length];
        System.arraycopy(header, 0, result, 0, header.length);
        return result;
    }

    public byte[] toBytes() {
        byte[] result = new byte[2 * header.length];
        for (int i = 0; i < header.length; i++) {
            result[2 * i] = (byte) ((header[i] >> 8) & 0xff);
            result[2 * i + 1] = (byte) (header[i] & 0xff);
        }
        return result;
    }

    public static final class Count {

        private final char count;

        public Count(int count) {
            this((char) count);
        }

        public Count(char count) {
            this.count = count;
        }

        public char toChar() {
            return count;
        }

    }

    public static final class Id {

        private final char id;

        public Id(char id) {
            this.id = id;
        }

        public char toChar() {
            return id;
        }
    }

    public static final class FlagsAndCodes {

        private final char flagsAndCodes;

        public FlagsAndCodes() {
            this((char) 0);
        }

        private FlagsAndCodes(char flagsAndCodes) {
            this.flagsAndCodes = flagsAndCodes;
        }

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
            return new FlagsAndCodes((char) ((flagsAndCodes | mask) ^ mask | responseCode.toByte()));
        }

        public char toChar() {
            return this.flagsAndCodes;
        }

    }

    public static final class ResponseCode {

        private final byte responseCode;

        public ResponseCode(int responseCode) {
            this.responseCode = (byte) (responseCode & 0x0f);
        }

        public byte toByte() {
            return this.responseCode;
        }
    }

    public static final class BytesHeader extends Header {

        public BytesHeader(byte[] bytes) {
            super(filled(bytes));
        }

        private static char[] filled(byte[] bytes) {
            char[] result = new char[6];
            for (int i = 0; i < result.length; i++) {
                result[i] = (char) (((bytes[2 * i] & 0xff) << 8) | (bytes[2 * i + 1] & 0xff));
            }
            return result;
        }

    }

}
