#!/bin/bash
target_dir=${HOME}/hpServer

PID=`cat ${target_dir}/hp-server.pid`
kill $PID

exit 0

