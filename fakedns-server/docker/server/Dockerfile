FROM openjdk:7-jre-alpine
MAINTAINER Mike S. <silvanovich.michael@gmail.com>
RUN mkdir -p /var/fakedns-server
ADD fakedns-server-1.0-SNAPSHOT.jar /var/fakedns-server/fakedns-server-1.0-SNAPSHOT.jar
CMD java -jar /var/fakedns-server/fakedns-server-1.0-SNAPSHOT.jar 0.0.0.0 8099
