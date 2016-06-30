package ru.hardcoders.dns;

import ru.hardcoders.dns.transport.CompressedResponse;
import ru.hardcoders.dns.transport.Header;
import ru.hardcoders.dns.transport.QueryMessage;

import java.net.InetAddress;

/**
 * Created by silvmike on 30.06.16.
 */
public class DNS {

    private final CompressedResponse.TimeToLive timeToLive;
    private final Registry registry;

    public DNS(CompressedResponse.TimeToLive timeToLive) {
        this(new Registry(), timeToLive);
    }

    public DNS(Registry registry, CompressedResponse.TimeToLive timeToLive) {
        this.registry = registry;
        this.timeToLive = timeToLive;
    }

    public CompressedResponse response(QueryMessage message) {

        InetAddress address = registry.resolve(message.question());
        Header messageHeader = message.header();

        if (address != null) {

            Header.FlagsAndCodes flagsAndCodes = messageHeader.flagsAndCodes()
                                                              .withResponse()
                                                              .withResponseCode(CompressedResponse.ErrorCode.NO_ERROR.code());

            Header responseHeader = messageHeader.withFlagsAndCodes(flagsAndCodes);

            responseHeader = responseHeader.withAnswerRecordCount(new Header.Count(1))
                                           .withAuthorityRecordCount(new Header.Count(0))
                                           .withAdditionalRecordCount(new Header.Count(0));

            CompressedResponse.CompressedAnswer answer = new CompressedResponse.CompressedAnswer();
            answer = answer.withData(new CompressedResponse.Data(address))
                           .withNamePointer(new CompressedResponse.NamePointer())
                           .withTTL(timeToLive)
                           .withClass(new CompressedResponse.InternetClass())
                           .withType(new CompressedResponse.InternetType());

            return new CompressedResponse(message).withHeader(responseHeader)
                                                  .withAnswer(answer);

        } else {

            Header.FlagsAndCodes flagsAndCodes = messageHeader.flagsAndCodes()
                                                              .withResponse()
                                                              .withResponseCode(CompressedResponse.ErrorCode.DOMAIN_NOT_FOUND.code());

            Header responseHeader = messageHeader.withFlagsAndCodes(flagsAndCodes);

            responseHeader = responseHeader.withAnswerRecordCount(new Header.Count(0))
                                           .withAuthorityRecordCount(new Header.Count(0))
                                           .withAdditionalRecordCount(new Header.Count(0));

            return new CompressedResponse(message).withHeader(responseHeader)
                                                  .withNoAnswer();

        }

    }
}
