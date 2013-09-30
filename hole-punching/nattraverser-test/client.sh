#!/bin/bash

set -e
function help_exit {
    echo ""
    echo "Usage: $0 MY_ID]"
    echo " e.g.: $0 3"
    echo ""
    exit 1
}

if [ $# -eq 0 ]  ; then
   help_exit
fi

rm /tmp/nattraverser.log

echo "Client ID is $1"

ID=$1

shift

java -jar deploy/hp.jar false $ID 0 cloud5.sics.se false $@
