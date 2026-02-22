package ru.hardcoders.dns.impl;

import ru.hardcoders.dns.api.DNS;
import ru.hardcoders.dns.impl.registry.InvalidationAwareRegistryImpl;
import ru.hardcoders.dns.impl.transport.CompressedResponse;
import ru.hardcoders.dns.impl.transport.Header;
import ru.hardcoders.dns.impl.transport.QueryMessage;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CachingDNS implements DNS, InvalidationAwareRegistryImpl.InvalidationListener {

    private final DNS dns;
    private final ConcurrentMap<String, CompressedResponse> cache = new ConcurrentHashMap<>();

    public CachingDNS(DNS dns) {
        this.dns = dns;
    }

    @Override
    public CompressedResponse response(QueryMessage message) {
        var response = getResponseInternal(message);
        Header messageHeader = message.header();

        if (response.found()) {

            Header.FlagsAndCodes flagsAndCodes = messageHeader.flagsAndCodes()
                    .withResponse()
                    .withResponseCode(CompressedResponse.ErrorCode.NO_ERROR.code());

            Header responseHeader = messageHeader.withFlagsAndCodes(flagsAndCodes);

            responseHeader = responseHeader.withAnswerRecordCount(new Header.Count(1))
                    .withAuthorityRecordCount(new Header.Count(0))
                    .withAdditionalRecordCount(new Header.Count(0));

            return new CompressedResponse(response.message(), true).withHeader(responseHeader);
        } else {

            Header.FlagsAndCodes flagsAndCodes = messageHeader.flagsAndCodes()
                                                              .withResponse()
                                                              .withResponseCode(CompressedResponse.ErrorCode.DOMAIN_NOT_FOUND.code());

            Header responseHeader = messageHeader.withFlagsAndCodes(flagsAndCodes);

            responseHeader = responseHeader.withAnswerRecordCount(new Header.Count(0))
                                           .withAuthorityRecordCount(new Header.Count(0))
                                           .withAdditionalRecordCount(new Header.Count(0));

            return new CompressedResponse(message, false).withHeader(responseHeader).withNoAnswer();
        }
    }

    private CompressedResponse getResponseInternal(QueryMessage message) {
        return cache.computeIfAbsent(message.question(), q -> {
            var response = dns.response(message);
            return response.found() ? response : null;
        });
    }

    @Override
    public void onChange(String hostname) {
        cache.remove(hostname);
    }
}
