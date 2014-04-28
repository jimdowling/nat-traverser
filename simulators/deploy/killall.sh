#!/bin/bash
col=2
if [ $# -eq 1 ] ; then
 col=$1 
fi
ps -ef | grep croupier | cut -d " " -f $col | while read line ; do kill $line ; done
