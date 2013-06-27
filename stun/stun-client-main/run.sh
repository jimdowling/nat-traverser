#!/bin/bash
if [ $# -lt 3 ] ; then
 echo "usage: <prog> server upnp 0|1|2 (publicIp/privIp|tenDotIp1|tenDotIp2) [natBinding] [clientPort]"
 echo "Example: <prog> cloud7.sics.se true"
 exit 1
fi

java -jar target/stun-client-main-1.0-SNAPSHOT-jar-with-dependencies.jar $@
