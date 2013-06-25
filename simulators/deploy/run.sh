#!/bin/bash

if [ $# -ne 10 ] ; then
 echo "usage: <prog> scenarioName numPub numPriv localHistorySz neighbourHistorySz numReplications expLength nodeSelection experimentId experimentIteration"
 echo "scenarioName = {join, changing-ratio, churn, failure}"
 exit
fi

rm out-*
rm simulation*.log*
rm -rf unprocessed/*

JAVA_OPTS="-Xmx10g -Xms1g"
# -Xmn1g -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:SurvivorRatio=8 -XX:TargetSurvivorRatio=90 -XX:MaxTenuringThreshold=31"

NUM_NODES=`expr $2 + $3`

c=0
max=1000
while [ $c -lt $6 ] ; do
# generate random number
 seed="$((`cat /dev/urandom | od -N1 -An -i` % $max))"
 rm out-$seed > /dev/null 2>&1
 echo "SEED is $seed"
 it=`expr ${10} + $c`
 echo "Starting gvod-simulator.jar $seed $1 $2 $3 $4 $5 $7 $8 $9 $it"
 java -jar $JAVA_OPTS gvod-simulator.jar $seed $1 $2 $3 $4 $5 $7 $8 $9 $it > /dev/null &
 c=`expr $c + 1`
 echo "$seed " >> seed-$9-$it
done

echo "$1-$2-$3-$4-$5-$8" > experiment
echo "Did you remember to './clean.sh'?"
exit
