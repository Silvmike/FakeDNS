package ru.hardcoders.dns.transport;

import java.net.InetAddress;

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
        byte[] message = new byte[this.message.length];
        System.arraycopy(this.message, 0, message, 0, message.length);
        System.arraycopy(header, 0, message, 0, header.length);
        return new CompressedResponse(message);
    }

    public CompressedResponse withAnswer(CompressedAnswer compressedAnswer) {
        byte[] answer = compressedAnswer.toByte();
        int endOfMessage = endOfQuestion() + 4;
        byte[] message = new byte[endOfMessage + answer.length + 1];
        System.arraycopy(this.message, 0, message, 0, endOfMessage + 1);
        System.arraycopy(answer, 0, message, endOfMessage + 1, answer.length);
        return new CompressedResponse(message);
    }

    public CompressedResponse withNoAnswer() {
        int endOfMessage = endOfQuestion() + 1;
        byte[] message = new byte[endOfMessage];
        System.arraycopy(this.message, 0, message, 0, endOfMessage);
        return new CompressedResponse(message);
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
        byte[] message = new byte[this.message.length];
        System.arraycopy(this.message, 0, message, 0, message.length);
        return message;
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

    public static class CompressedAnswer {

        private final char[] answer;

        public CompressedAnswer() {
            this(new char[0]);
        }

        private CompressedAnswer(char[] answer) {
            this.answer = answer;
        }

        public CompressedAnswer withNamePointer(NamePointer namePointer) {
            char[] answer = new char[this.answer.length];
            char[] pointer = namePointer.toChar();
            if (answer.length < pointer.length) {
                answer = new char[pointer.length];
            }
            System.arraycopy(this.answer, 0, answer, 0, this.answer.length);
            System.arraycopy(pointer, 0, answer, 0, pointer.length);
            return new CompressedAnswer(answer);
        }

        public CompressedAnswer withType(Type answerType) {
            char[] answer = new char[this.answer.length];
            char[] type = answerType.toChar();
            if (answer.length < type.length + 1) {
                answer = new char[type.length + 1];
            }
            System.arraycopy(this.answer, 0, answer, 0, this.answer.length);
            System.arraycopy(type, 0, answer, 1, type.length);
            return new CompressedAnswer(answer);
        }

        public CompressedAnswer withClass(AnswerClass answerClass) {
            char[] answer = new char[this.answer.length];
            char[] clazz = answerClass.toChar();
            if (answer.length < clazz.length + 2) {
                answer = new char[clazz.length + 2];
            }
            System.arraycopy(this.answer, 0, answer, 0, this.answer.length);
            System.arraycopy(clazz, 0, answer, 2, clazz.length);
            return new CompressedAnswer(answer);
        }

        public CompressedAnswer withTTL(TimeToLive timeToLive) {
            char[] answer = new char[this.answer.length];
            char[] ttl = timeToLive.toChar();
            if (answer.length < ttl.length + 3) {
                answer = new char[ttl.length + 3];
            }
            System.arraycopy(this.answer, 0, answer, 0, this.answer.length);
            System.arraycopy(ttl, 0, answer, 3, ttl.length);
            return new CompressedAnswer(answer);
        }

        public CompressedAnswer withData(Data resourceData) {
            char[] answer = new char[this.answer.length];
            char[] data = resourceData.toChar();
            if (answer.length < data.length + 5) {
                answer = new char[data.length + 5];
            }
            System.arraycopy(this.answer, 0, answer, 0, this.answer.length);
            System.arraycopy(data, 0, answer, 5, data.length);
            return new CompressedAnswer(answer);
        }

        public char[] toChar() {
            char[] result = new char[answer.length];
            System.arraycopy(answer, 0, result, 0, result.length);
            return result;
        }

        public byte[] toByte() {
            byte[] result = new byte[answer.length * 2];
            for (int i = 0; i < answer.length; i++) {
                result[2 * i] = (byte) ((answer[i] >> 8) & 0xff);
                result[2 * i + 1] = (byte) (answer[i] & 0xff);
            }
            return result;
        }

    }

    public static final class NamePointer {

        private final char[] pointer;

        public NamePointer() {
            this.pointer = new char[]{0xc000 + DNS_HEADER_LENGTH};
        }

        public char[] toChar() {
            char[] data = new char[this.pointer.length];
            System.arraycopy(this.pointer, 0, data, 0, data.length);
            return data;
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

    public static class Type {

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
    }

    public static class AnswerClass {

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
    }

    public static class TimeToLive {

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
            result[1] = (byte) ((ttl >> 24) & 0xff);
            result[2] = (byte) ((ttl >> 16) & 0xff);
            result[3] = (byte) ((ttl >> 8) & 0xff);
            result[4] = (byte) ((ttl) & 0xff);
            return result;
        }

    }

    public static final class Data {

        private final char[] data;

        public Data(InetAddress address) {
            char[] data = new char[3];
            byte[] addr = address.getAddress();
            data[0] = 4;
            data[1] = (char) ((addr[0] << 8) | addr[1]);
            data[2] = (char) ((addr[2] << 8) | addr[3]);
            this.data = data;
        }

        public char[] toChar() {
            char[] data = new char[this.data.length];
            System.arraycopy(this.data, 0, data, 0, data.length);
            return data;
        }

    }

}
