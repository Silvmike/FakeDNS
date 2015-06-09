#!/bin/sh
DNS_SERVER=$(docker inspect --format '{{ .NetworkSettings.IPAddress }}' fakedns-server)
docker run -d --link=fakedns-server:fakedns.hardcoders.ru \
           -h client1.hardcoders.ru --dns=$DNS_SERVER silvmike/fakedns-client

docker run -d --link=fakedns-server:fakedns.hardcoders.ru \
           -h client2.hardcoders.ru --dns=$DNS_SERVER silvmike/fakedns-client

docker run -d --link=fakedns-server:fakedns.hardcoders.ru \
           -h client3.hardcoders.ru --dns=$DNS_SERVER silvmike/fakedns-client
