package ru.hardcoders.dns;

import java.net.InetAddress;

/**
 * Created by root on 08.06.15.
 */
public class DNSAnswerHelper {

    private static final int QUESTION_CLASS_PLUS_QUESTION_TYPE_FIELDS = 4 /* bytes */;
    private static final int QUESTION_NAME_POINTER_LENGTH = 2 /* bytes */;
    private static final int ANSWER_DATA_LENGTH = 4 /* bytes */; // for now only for IPv4
    private static final int ANSWER_RR_LENGTH = QUESTION_NAME_POINTER_LENGTH + 10 /* bytes */ + ANSWER_DATA_LENGTH;
    public static final int DEFAULT_TTL_SECONDS = 5;


    private final DNSMessage originalMessage;
    private final byte[] message;
    private DNSHeaderHelper.ErrorCode errorCode;
    private InetAddress address;
    private int timeToLiveSeconds = DEFAULT_TTL_SECONDS;

    public DNSAnswerHelper(DNSMessage originalMessage) {
        this.originalMessage = originalMessage;
        this.message = originalMessage.getMessage();
    }

    public void setErrorCode(DNSHeaderHelper.ErrorCode errorCode) {
        this.errorCode = errorCode;
        new DNSHeaderHelper(this.message).setResponseCode(errorCode);
    }


    public void setAddress(InetAddress address) {
        this.address = address;
        //byte[] addressBytes = address.getAddress();
        //System.arraycopy(addressBytes, 0, this.result, this.result.length - addressBytes.length, addressBytes.length);
    }

    public void setTimeToLiveSeconds(int timeToLiveSeconds) {
        this.timeToLiveSeconds = timeToLiveSeconds;
    }

    public byte[] build() {
        byte[] result = null;
        DNSHeaderHelper headerHelper = new DNSHeaderHelper(this.message);
        headerHelper.setResponse(true);
        switch (errorCode) {
            case NO_ERROR:
                headerHelper.setAnswerCount((short) 1);
                headerHelper.setAuthorityCount((short) 0);
                headerHelper.setAdditionalCount((short) 0);
                result = new byte[this.originalMessage.getEndOfQuestionPosition() + 1
                                         + QUESTION_CLASS_PLUS_QUESTION_TYPE_FIELDS
                                         + ANSWER_RR_LENGTH];
                System.arraycopy(this.message, 0, result, 0, Math.min(this.message.length, result.length));

                // set pointer to related question name
                int pos = this.originalMessage.getEndOfQuestionPosition() + QUESTION_CLASS_PLUS_QUESTION_TYPE_FIELDS + 1;
                result[pos] = (byte) 0xc0; // means 1100 0000 (see http://www.zytrax.com/books/dns/ch15/)
                result[pos + 1] = (byte) DNSMessage.DNS_HEADER_LENGTH; // offset from beginning of the message
                // set rr type = 1
                result[pos + 2] = 0;
                result[pos + 3] = 1;
                // set rr class = 1 (internet class)
                result[pos + 4] = 0;
                result[pos + 5] = 1;
                // set time to live
                result[pos + 6] = (byte)((timeToLiveSeconds >> 24) & 0xff);
                result[pos + 7] = (byte)((timeToLiveSeconds >> 16) & 0xff);
                result[pos + 8] = (byte)((timeToLiveSeconds >> 8) & 0xff);
                result[pos + 9] = (byte)((timeToLiveSeconds) & 0xff);
                // set data length
                result[pos + 10] = 0;
                result[pos + 11] = ANSWER_DATA_LENGTH;
                // set ip address
                System.arraycopy(address.getAddress(), 0, result, pos + 12, ANSWER_DATA_LENGTH);

                break;

            case DOMAIN_NOT_FOUND:

                headerHelper.setAnswerCount((short) 0);
                headerHelper.setAuthorityCount((short) 0);
                headerHelper.setAdditionalCount((short) 0);
                result = new byte[originalMessage.getEndOfQuestionPosition() + 1 + QUESTION_CLASS_PLUS_QUESTION_TYPE_FIELDS]; // only header + question block
                System.arraycopy(this.message, 0, result, 0, result.length);

                break;

            default:
                throw new UnsupportedOperationException("not implemented yet!");
        }
        return result;
    }
}
