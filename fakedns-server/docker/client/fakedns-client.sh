#!/bin/bash

if [ "$#" -ne 3 ]; then
  echo "You must specify 3 args: registrator hostname, registrator port, your fake host name!"
  exit 
fi

echo "$3
" | netcat $1 $2

echo OK
