FROM maven:3-jdk-8-slim as jarBuilder
WORKDIR /tmp/server
COPY src/ src/
COPY pom.xml .
RUN mvn package

FROM oracle/graalvm-ce:19.1.0 as executableBuilder
WORKDIR /tmp/server/target
RUN gu install native-image
COPY --from=jarBuilder /tmp/server/target/fakedns-server.jar  .
RUN native-image --static -H:+ReportExceptionStackTraces -H:+ReportUnsupportedElementsAtRuntime -jar fakedns-server.jar

FROM sequenceiq/alpine-dev:3.1 as tester
WORKDIR /tmp/target
COPY --from=executableBuilder /tmp/server/target/fakedns-server  .
COPY tests.sh .
RUN chmod +x tests.sh
RUN chmod +x fakedns-server
RUN ./tests.sh

FROM alpine:3.5
WORKDIR /var/fakedns-server
ENV FAKE_DNS_OPTS -Xmx1m  0.0.0.0 8099
COPY --from=executableBuilder /tmp/server/target/fakedns-server  .
RUN chmod +x fakedns-server
CMD ./fakedns-server $FAKE_DNS_OPTS
LABEL maintainer="Mikhail Silvanovich <silvanovich.michael@gmail.com>"