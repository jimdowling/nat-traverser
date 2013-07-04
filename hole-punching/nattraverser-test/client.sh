#!/bin/bash

function help_exit {
    echo ""
    echo "Usage: $0 MY_ID]"
    echo " e.g.: $0 3"
    echo ""
    exit 1
}

if [ $# -eq 0 ]  || [ $# -eq 2 ] ; then
   help_exit
fi

ID=$1
shift
if [ $# -gt 1 ] ; then
    java -jar deploy/hp.jar false $ID 0 cloud7.sics.se false $@
elif [ $# -eq 0 ] ; then 
    java -jar deploy/hp.jar false $ID 0 cloud7.sics.se false
    echo ""
fi

