FROM maven:3-eclipse-temurin-21-alpine as jarBuilder
WORKDIR /tmp/server
COPY src/ src/
COPY pom.xml .
RUN mvn package
RUN mkdir toolchain && wget https://more.musl.cc/10/x86_64-linux-musl/x86_64-linux-musl-native.tgz && tar -zxf x86_64-linux-musl-native.tgz -C toolchain
RUN wget https://zlib.net/zlib-1.3.2.tar.gz && tar -zxf zlib-1.3.2.tar.gz -C toolchain

FROM ghcr.io/graalvm/native-image-community:21 as executableBuilder
WORKDIR /tmp/server/target
COPY --from=jarBuilder /tmp/server/toolchain  .
ENV TOOLCHAIN_DIR=/tmp/server/target/x86_64-linux-musl-native
ENV CC=$TOOLCHAIN_DIR/bin/gcc
RUN ls $TOOLCHAIN_DIR
RUN cd $TOOLCHAIN_DIR/../zlib-1.3.2 && ./configure --prefix=$TOOLCHAIN_DIR --static && make && make install
COPY --from=jarBuilder /tmp/server/target/fakedns-server.jar  .
RUN PATH="$PATH:$TOOLCHAIN_DIR/bin" native-image --static --libc=musl -H:+ReportExceptionStackTraces -H:+ReportUnsupportedElementsAtRuntime -jar fakedns-server.jar
RUN chmod +x fakedns-server

FROM sequenceiq/alpine-dev:3.1 as tester
WORKDIR /tmp/target
COPY --from=executableBuilder /tmp/server/target/fakedns-server  .
COPY tests.sh .
RUN chmod +x tests.sh
RUN ./tests.sh

FROM scratch
COPY --from=executableBuilder /tmp/server/target/fakedns-server  .
ENTRYPOINT ["./fakedns-server", "-Xmx1m", "0.0.0.0", "8099"]
LABEL maintainer="Mikhail Silvanovich <silvanovich.michael@gmail.com>"
