#!/bin/bash

for host in cloud1.sics.se cloud2.sics.se  cloud3.sics.se  cloud4.sics.se  cloud5.sics.se  cloud6.sics.se  cloud7.sics.se lucan.sics.se
do 
echo "Downloading results from $host"
#if [ ! -d results/$host ] ; then
#  mkdir results/$host
#fi
scp -r jdowling@$host:~/croupier-deploy/results/* results/
done
