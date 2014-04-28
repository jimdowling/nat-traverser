#!/bin/bash
p=$HOME
target_dir="${p}/hpServer"

nohup java -jar ${target_dir}/hp.jar  $@ > ${target_dir}/hp.log &
echo $! > ${target_dir}/hp-server.pid

