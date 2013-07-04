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

echo "Client ID is $1"

java -jar deploy/hp.jar false $1 0 cloud7.sics.se false $@

