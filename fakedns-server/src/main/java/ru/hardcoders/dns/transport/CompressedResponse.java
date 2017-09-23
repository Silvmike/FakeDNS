package ru.hardcoders.dns.transport;

import java.net.InetAddress;
import java.util.Arrays;

/**
 * Created by silvmike on 30.06.16.
 */
public class CompressedResponse {

    private static final int DNS_HEADER_LENGTH = 12;

    private final byte[] message;

    public CompressedResponse(QueryMessage message) {
        this(message.toBytes());
    }

    private CompressedResponse(byte[] message) {
        this.message = message;
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

    public static class CompressedAnswer implements CharArraySerializable {

        private final char[] answer;

        public CompressedAnswer() {
            this(new char[0]);
        }

        private CompressedAnswer(char[] answer) {
            this.answer = answer;
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

    public static final class NamePointer implements CharArraySerializablePositionAware {

        private final char[] pointer;

        public NamePointer() {
            this.pointer = new char[]{0xc000 + DNS_HEADER_LENGTH};
        }

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

    public static final class InternetType extends Type {

        public InternetType() {
            super(1);
        }
    }

    public static final class InternetClass extends AnswerClass {

        public InternetClass() {
            super(1);
        }
    }

    public static class Type implements CharArraySerializablePositionAware {

        private final char type;

        public Type(int type) {
            this((char) type);
        }

        public Type(char type) {
            this.type = type;
        }

        public char[] toChar() {
            return new char[]{this.type};
        }

        @Override
        public int pos() {
            return 1;
        }
    }

    public static class AnswerClass implements CharArraySerializablePositionAware {

        private final char answerClass;

        public AnswerClass(int answerClass) {
            this((char) answerClass);
        }

        public AnswerClass(char answerClass) {
            this.answerClass = answerClass;
        }

        public char[] toChar() {
            return new char[]{this.answerClass};
        }

        @Override
        public int pos() {
            return 2;
        }
    }

    public static class TimeToLive implements CharArraySerializablePositionAware {

        private final int ttl;

        public TimeToLive(int ttl) {
            this.ttl = ttl;
        }

        public int seconds() {
            return this.ttl;
        }

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

    public static final class Data implements CharArraySerializablePositionAware {

        private final char[] data;

        public Data(InetAddress address) {
            char[] data = new char[3];
            byte[] addr = address.getAddress();
            data[0] = 4;
            data[1] = (char) ((addr[0] << 8) | (addr[1] & 0xff));
            data[2] = (char) ((addr[2] << 8) | (addr[3] & 0xff));
            this.data = data;
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
