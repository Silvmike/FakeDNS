FROM phusion/baseimage
MAINTAINER Mike S. <silvanovich.michael@gmail.com>
ADD pre_configure.sh /tmp/pre_configure.sh
RUN sh /tmp/pre_configure.sh && rm /tmp/pre_configure.sh
RUN apt-get update && apt-get --assume-yes install dnsutils iputils-ping netcat
RUN mkdir -p /etc/my_init.d && mkdir -p /var/fakedns-client
ADD fakedns-client.sh /var/fakedns-client/fakedns-client.sh
ADD fakedns-register.sh /etc/my_init.d/fakedns-register.sh
RUN chmod 775 /etc/my_init.d/fakedns-register.sh
RUN touch /etc/apt/apt.conf.d/01proxy && rm /etc/apt/apt.conf.d/01proxy
