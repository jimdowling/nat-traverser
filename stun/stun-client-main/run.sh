#!/bin/bash
if [ $# -lt 2 ] ; then
 echo "usage: <prog> server upnp [clientPort]"
 echo "Example: <prog> cloud7.sics.se true"
 exit 1
fi

java -jar target/stun-client-main-1.0-SNAPSHOT-jar-with-dependencies.jar $@
