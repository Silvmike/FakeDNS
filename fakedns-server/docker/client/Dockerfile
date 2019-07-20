FROM sequenceiq/alpine-dev:3.1 as tester
MAINTAINER Mike S. <silvanovich.michael@gmail.com>
RUN ln -s /usr/bin/nc /usr/bin/netcat
RUN mkdir -p /var/fakedns-client
ADD fakedns-client.sh /var/fakedns-client/fakedns-client.sh
ADD entry.sh /var/fakedns-client/entry.sh
CMD /bin/sh /var/fakedns-client/entry.sh
