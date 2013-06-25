#!/bin/bash
if [ $# -ne 2 ] ; then
 echo "usage: <prog> server preferPrivateIp (true|false)"
 exit 1
fi
nohup java -jar target/stun-server-main-1.0-SNAPSHOT-jar-with-dependencies.jar $@ 2,1 > stun-server.log &
echo $! > ./stun.pid

