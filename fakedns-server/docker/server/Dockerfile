FROM phusion/baseimage
MAINTAINER Mike S. <silvanovich.michael@gmail.com>
RUN apt-get update
ADD pre_configure.sh /tmp/pre_configure.sh
RUN sh /tmp/pre_configure.sh && rm /tmp/pre_configure.sh
RUN apt-get --assume-yes install jvm-7-avian-jre telnet
RUN mkdir -p /var/fakedns-server && mkdir -p /etc/my_init.d
ADD fakedns-server-1.0-SNAPSHOT.jar /var/fakedns-server/fakedns-server-1.0-SNAPSHOT.jar
ADD fakedns-server /etc/init.d/fakedns-server
ADD fakedns-server.sh /etc/my_init.d/fakedns-server.sh
RUN chmod 775 /etc/init.d/fakedns-server
RUN chmod 775 /etc/my_init.d/fakedns-server.sh
RUN touch /etc/apt/apt.conf.d/01proxy && rm /etc/apt/apt.conf.d/01proxy
