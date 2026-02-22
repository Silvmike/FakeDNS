package ru.hardcoders.dns.impl.transport;

import java.net.InetAddress;
import java.util.Arrays;

/**
 * Created by silvmike on 30.06.16.
 */
public record CompressedResponse(byte[] message) {

    private static final int DNS_HEADER_LENGTH = 12;

    public CompressedResponse(QueryMessage message) {
        this(message.toBytes());
    }

    public CompressedResponse withHeader(Header dnsHeader) {
        byte[] header = dnsHeader.toBytes();
        byte[] message = Arrays.copyOf(this.message, this.message.length);
        System.arraycopy(header, 0, message, 0, header.length);
        return new CompressedResponse(message);
    }

    public CompressedResponse withAnswer(CompressedAnswer compressedAnswer) {
        int endOfMessage = endOfQuestion() + 4;
        byte[] answer = compressedAnswer.toBytes();
        byte[] message = Arrays.copyOf(this.message, endOfMessage + answer.length + 1);
        System.arraycopy(answer, 0, message, endOfMessage + 1, answer.length);
        return new CompressedResponse(message);
    }

    public CompressedResponse withNoAnswer() {
        int endOfMessage = endOfQuestion() + 1;
        return new CompressedResponse(
            Arrays.copyOf(this.message, endOfMessage)
        );
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

    public byte[] toBytes() {
        return Arrays.copyOf(this.message, this.message.length);
    }

    public enum ErrorCode {

        NO_ERROR(0),
        FORMAT_ERROR(1),
        SERVER_ERROR(2),
        DOMAIN_NOT_FOUND(3),
        NOT_IMPLEMENTED(4),
        REFUSED(5);

        private final byte code;


        ErrorCode(int code) {
            this.code = (byte) (0x80 & code);
        }

        public Header.ResponseCode code() {
            return new Header.ResponseCode(code);
        }
    }

    public record CompressedAnswer(char[] answer) implements CharArraySerializable {

        public CompressedAnswer() {
            this(new char[0]);
        }

        public CompressedAnswer withNamePointer(NamePointer namePointer) {
            return new CompressedAnswer(
                applyPositionAware(namePointer)
            );
        }

        public CompressedAnswer withType(Type answerType) {
            return new CompressedAnswer(
                applyPositionAware(answerType)
            );
        }

        public CompressedAnswer withClass(AnswerClass answerClass) {
            return new CompressedAnswer(
                applyPositionAware(answerClass)
            );
        }

        public CompressedAnswer withTTL(TimeToLive timeToLive) {
            return new CompressedAnswer(
                applyPositionAware(timeToLive)
            );
        }

        public CompressedAnswer withData(Data resourceData) {
            return new CompressedAnswer(
                applyPositionAware(resourceData)
            );
        }

        private char[] applyPositionAware(CharArraySerializablePositionAware field) {
            char[] serialized = field.toChar();
            char[] answer = Arrays.copyOf(this.answer, Math.max(this.answer.length, serialized.length + field.pos()));
            System.arraycopy(serialized, 0, answer, field.pos(), serialized.length);
            return answer;
        }

        public char[] toChar() {
            char[] result = new char[answer.length];
            System.arraycopy(answer, 0, result, 0, result.length);
            return result;
        }

        public byte[] toBytes() {
            byte[] result = new byte[answer.length * 2];
            for (int i = 0; i < answer.length; i++) {
                result[2 * i] = (byte) ((answer[i] >> 8) & 0xff);
                result[2 * i + 1] = (byte) (answer[i] & 0xff);
            }
            return result;
        }

    }

    public record NamePointer(char[] pointer) implements CharArraySerializablePositionAware {

        public static NamePointer create() {
            return new NamePointer(new char[]{0xc000 + DNS_HEADER_LENGTH});
        }

        @Override
        public char[] toChar() {
            char[] data = new char[this.pointer.length];
            System.arraycopy(this.pointer, 0, data, 0, data.length);
            return data;
        }

        @Override
        public int pos() {
            return 0;
        }
    }

    public record InternetType(Type type) implements CharArraySerializablePositionAware {

        public InternetType() {
            this(new Type(1));
        }

        @Override
        public char[] toChar() {
            return type.toChar();
        }

        @Override
        public int pos() {
            return type.pos();
        }
    }

    public record InternetClass(AnswerClass answerClass) implements CharArraySerializablePositionAware {

        public InternetClass() {
            this(new AnswerClass(1));
        }

        @Override
        public char[] toChar() {
            return answerClass.toChar();
        }

        @Override
        public int pos() {
            return answerClass.pos();
        }
    }

    public record Type(char type) implements CharArraySerializablePositionAware {

        public Type(int type) {
            this((char) type);
        }

        @Override
        public char[] toChar() {
            return new char[]{this.type};
        }

        @Override
        public int pos() {
            return 1;
        }
    }

    public record AnswerClass(char answerClass) implements CharArraySerializablePositionAware {

        public AnswerClass(int answerClass) {
            this((char) answerClass);
        }

        @Override
        public char[] toChar() {
            return new char[]{this.answerClass};
        }

        @Override
        public int pos() {
            return 2;
        }
    }

    public record TimeToLive(int ttl) implements CharArraySerializablePositionAware {

        public int seconds() {
            return this.ttl;
        }

        @Override
        public char[] toChar() {
            char[] result = new char[2];
            result[0] = (char) ((ttl >> 16) & 0xffff);
            result[1] = (char) ((ttl) & 0xffff);
            return result;
        }

        public byte[] bytes() {
            byte[] result = new byte[4];
            result[0] = (byte) ((ttl >> 24) & 0xff);
            result[1] = (byte) ((ttl >> 16) & 0xff);
            result[2] = (byte) ((ttl >> 8) & 0xff);
            result[3] = (byte) ((ttl) & 0xff);
            return result;
        }

        @Override
        public int pos() {
            return 3;
        }
    }

    public record Data(char[] data) implements CharArraySerializablePositionAware {

        public Data(InetAddress address) {
            this(fromInetAddress(address));
        }

        private static char[] fromInetAddress(InetAddress address) {
            char[] data = new char[3];
            byte[] addr = address.getAddress();
            data[0] = 4;
            data[1] = (char) ((addr[0] << 8) | (addr[1] & 0xff));
            data[2] = (char) ((addr[2] << 8) | (addr[3] & 0xff));
            return data;
        }

        public char[] toChar() {
            char[] data = new char[this.data.length];
            System.arraycopy(this.data, 0, data, 0, data.length);
            return data;
        }

        public int pos() {
            return 5;
        }
    }

    interface PositionAware {
        int pos();
    }

    interface CharArraySerializable {
        char[] toChar();
    }

    interface CharArraySerializablePositionAware extends PositionAware, CharArraySerializable {}

}
