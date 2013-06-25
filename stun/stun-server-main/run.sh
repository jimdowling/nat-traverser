#!/bin/bash
if [ $# -ne 2 ] ; then
 echo "usage: <prog> server 0|1|2|3 (public|private1|private2 ip addr)"
 exit 1
fi
nohup java -jar target/stun-server-main-1.0-SNAPSHOT-jar-with-dependencies.jar $@ 2,1 > stun-server.log &
echo $! > ./stun.pid

