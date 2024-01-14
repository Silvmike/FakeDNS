#!/bin/bash

FAKE_DNS_REGISTRATION_PORT=8099
FAKE_DNS_HOST=127.0.0.1

register_hostname() {
    echo "$1
" | nc 127.0.0.1 8099
}

test_lookup() {
    DIG_RESULT=$(dig @$FAKE_DNS_HOST $1 | grep -A 1 "ANSWER SECTION" | tail -n 1 | awk '{print $1$3$4}')
    EXPECTED="$1.INA"
    if [ "$DIG_RESULT" != "$EXPECTED" ]; then
      echo "Expected <$EXPECTED> but was <$DIG_RESULT>"
      exit 1
    fi
}

test_register_lookup() {
    echo "Test <$1>: EXECUTING"
    register_hostname $1
    test_lookup $1
    echo "Test <$1>: SUCCESS"
}

echo Starting FakeDNS server...

./fakedns-server $FAKE_DNS_HOST $FAKE_DNS_REGISTRATION_PORT &>/dev/null &
PID=$!

echo Waiting for FakeDNS server startup...

sleep 5s # let server start, not so reliable though

echo Running tests...

set -e
testData=(
    "somehost.ru" 
    "www.test.org" 
    "more.subdomains.please.test.info"
)

for testHost in ${testData[*]}; do
  test_register_lookup $testHost
done

set +e
kill $PID