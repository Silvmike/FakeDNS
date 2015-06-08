package ru.hardcoders.dns;

/**
 * Created by root on 08.06.15.
 */
public class DNSHeaderHelper {

    private static final int FIRST_BIT_MASK = 0x80; // 0x80 = 1000 0000
    private static final int FOUR_BITS_MASK = 0x1f;
    private static final int FIRST_FLAG_INDEX = 2; // 3th byte
    private static final int LAST_FLAG_INDEX = 3; // 4th byte of header is the last flag byte
    private static final int QUESTION_COUNT_INDEX = 4; //  5th
    private static final int ANSWER_COUNT_INDEX = 6; // 7th
    private static final int AUTHORITY_COUNT_INDEX = 8; // 9th
    private static final int ADDITIONAL_COUNT_INDEX = 10; // 11th

    /**
     * There are following codes:
     *
     * - NOERROR (RCODE:0) : DNS Query completed successfully
     * - FORMERR (RCODE:1) : DNS Query Format Error
     * - SERVFAIL (RCODE:2) : Server failed to complete the DNS request
     * - NXDOMAIN (RCODE:3) : Domain name does not exist
     * - NOTIMP (RCODE:4) : Function not implemented
     * - REFUSED (RCODE:5) : The server refused to answer for the query
     * - YXDOMAIN (RCODE:6) : Name that should not exist, does exist
     * - XRRSET (RCODE:7) : RRset that should not exist, does exist
     * - NOTAUTH (RCODE:9) : Server not authoritative for the zone
     * - NOTZONE (RCODE:10) : Name not in zone
     *
     */
    public enum ErrorCode {

        NO_ERROR(0),
        FORMAT_ERROR(1),
        SERVER_ERROR(2),
        DOMAIN_NOT_FOUND(3),
        NOT_IMPLEMENTED(4),
        REFUSED(5);

        private final int code;


        ErrorCode(int code) {
            this.code = code;
        }

        public int to4bitCode() {
            return FOUR_BITS_MASK & code;
        }
    }

    private final byte[] message;

    public DNSHeaderHelper(byte[] message) {
        this.message = message;
    }

    public void setResponseCode(ErrorCode code) {
        byte lastFlagByte = this.message[LAST_FLAG_INDEX];
        this.message[LAST_FLAG_INDEX] = (byte)(((lastFlagByte | FOUR_BITS_MASK) ^ FOUR_BITS_MASK) | code.to4bitCode());
    }

    public void setQuestionCount(short count) {
        putShort(count, QUESTION_COUNT_INDEX);
    }

    public void setAnswerCount(short count) {
        putShort(count, ANSWER_COUNT_INDEX);
    }

    public void setAuthorityCount(short count) {
        putShort(count, AUTHORITY_COUNT_INDEX);
    }

    public void setAdditionalCount(short count) {
        putShort(count, ADDITIONAL_COUNT_INDEX);
    }

    public void setResponse(boolean isResponse) {
        int value = isResponse ? FIRST_BIT_MASK : 0; // 0x80 = 1000 0000
        this.message[FIRST_FLAG_INDEX] = (byte)(((this.message[FIRST_FLAG_INDEX] | FIRST_BIT_MASK) ^ FIRST_BIT_MASK) | FIRST_BIT_MASK);
    }

    private void putShort(short value, int pos) {
        this.message[pos] = (byte)((value >> 8) & 0xff);
        this.message[pos + 1] = (byte)(value & 0xff);
    }

    public byte[] getMessage() {
        return message;
    }

}
