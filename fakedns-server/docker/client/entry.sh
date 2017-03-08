#!/bin/bash
/bin/sh /var/fakedns-client/fakedns-client.sh fakedns.hardcoders.ru 8099 $(hostname)
tail -f /dev/null
