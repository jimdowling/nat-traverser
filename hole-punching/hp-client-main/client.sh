#!/bin/bash

function help_exit {
    echo ""
    echo "Usage: $0 MY_ID [DEST_ID DEST_NAT_TYPE]"
    echo " e.g.: $0 3"
    echo " e.g.: $0 4 3 NAT_EI_PP_PD"
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
    sleep 5
    echo ""
    echo "Connect to this node using hole-punching by running:"
    echo "Usage: $0 id NAT_TYPE"
    echo " e.g.: $0 ID NAT_EI_PP_PD"
    echo ""
    echo ""
fi

