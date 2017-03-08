#!/bin/sh

# build fakedns-server

cd ../../ && mvn clean package
cp target/*.jar docker/server/
cd docker/server

docker build -t silvmike/fakedns-server .
