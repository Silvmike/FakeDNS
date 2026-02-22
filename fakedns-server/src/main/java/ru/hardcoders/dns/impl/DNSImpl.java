package ru.hardcoders.dns.impl;

import ru.hardcoders.dns.api.DNS;
import ru.hardcoders.dns.api.registry.Registry;
import ru.hardcoders.dns.impl.transport.CompressedResponse;
import ru.hardcoders.dns.impl.transport.Header;
import ru.hardcoders.dns.impl.transport.QueryMessage;

import java.net.InetAddress;

/**
 * Created by silvmike on 30.06.16.
 */
public class DNSImpl implements DNS {

    private final CompressedResponse.TimeToLive timeToLive;
    private final Registry registry;

    public DNSImpl(Registry registry, int timeToLive) {
        this.registry = registry;
        this.timeToLive = new CompressedResponse.TimeToLive(timeToLive);
    }

    public DNSImpl(Registry registry, CompressedResponse.TimeToLive timeToLive) {
        this.registry = registry;
        this.timeToLive = timeToLive;
    }

    @Override
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
                           .withNamePointer(CompressedResponse.NamePointer.create())
                           .withTTL(timeToLive)
                           .withClass(new CompressedResponse.InternetClass().answerClass())
                           .withType(new CompressedResponse.InternetType().type());

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
