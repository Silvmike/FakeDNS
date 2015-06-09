#!/bin/sh

# build docker container using apt-get-cacher if possible

if [ -z "$USE_APT_PROXY" ]; then
 USE_APT_PROXY=0
fi
if [ "$USE_APT_PROXY" = "1" ]; then
 CACHER_IP=$(docker inspect --format '{{ .NetworkSettings.IPAddress }}' cacher)
 echo "echo \"Acquire::http::Proxy \\\"http://$CACHER_IP:3142\\\";\" > /etc/apt/apt.conf.d/01proxy" > pre_configure.sh
else
 echo "echo \"NO APT-GET PROXY MODE\"" > pre_configure.sh
fi

docker build -t silvmike/fakedns-client .
