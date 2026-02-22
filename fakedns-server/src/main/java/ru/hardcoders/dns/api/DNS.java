package ru.hardcoders.dns.api;

import ru.hardcoders.dns.impl.transport.CompressedResponse;
import ru.hardcoders.dns.impl.transport.QueryMessage;

public interface DNS {

    CompressedResponse response(QueryMessage message);
}
