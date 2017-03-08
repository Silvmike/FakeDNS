FROM alpine:3.5
MAINTAINER Mike S. <silvanovich.michael@gmail.com>
RUN apk add --update netcat-openbsd bind-tools bash
RUN ln -s /usr/bin/nc /usr/bin/netcat
RUN mkdir -p /var/fakedns-client
ADD fakedns-client.sh /var/fakedns-client/fakedns-client.sh
ADD entry.sh /var/fakedns-client/entry.sh
CMD /bin/sh /var/fakedns-client/entry.sh
