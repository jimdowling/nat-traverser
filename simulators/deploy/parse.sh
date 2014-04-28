#!/bin/bash

SEEDS=seeds

for x in `cat seeds`
do
    FILE=out-$x
    root=unprocessed/$x

    if [ -d $root ] ; then
	rm -rf $root
    fi
    mkdir $root

    NUM_NODES=$root/num-nodes
    AVG_INDEGREE=$root/indegree
    AVG_PATH_LENGTH=$root/pathlength
    CC=$root/cc
    ESTIMATION=$root/estimation
    PUBLIC_ESTIMATION=$root/public-estimation
    PRIVATE_ESTIMATION=$root/private-estimation
    REAL_ESTIMATION=$root/real-distribution
    AVG_ERROR=$root/avg-err
    AVG_PUBLIC_ERROR=$root/avg-pub-err
    AVG_PRIVATE_ERROR=$root/avg-priv-err
    MAX_ERROR=$root/max-err
    MAX_PUBLIC_ERROR=$root/max-pub-err
    MAX_PRIVATE_ERROR=$root/max-priv-err
    MAX_PUBLIC_ESTIMATION=$root/max-pub-estimation
    MAX_PRIVATE_ESTIMATION=$root/max-priv-estimation
    MIN_PUBLIC_ESTIMATION=$root/min-pub-estimation
    MIN_PRIVATE_ESTIMATION=$root/min-priv-estimation
    SWITCHED_PARENTS=$root/switched
    OLD_PARENTS=$root/oldparents
    LATENCY=$root/latency
    RTT=$root/rtt
    NOT_REACHABLE=$root/notreachable
    PRIVATE_LOAD=$root/privateload
    PUBLIC_LOAD=$root/publicload

    cp $FILE $root/$FILE

    cat $FILE | egrep '^number of peers:' | sed -e 's/number of peers: //g' | awk '{print $1, $2}' > $NUM_NODES
    cat $FILE | egrep '^avg in-degree:' | sed -e 's/avg in-degree: //g' | awk '{print $1, $2}' > $AVG_INDEGREE
    cat $FILE | egrep '^avg path length:' | sed -e 's/avg path length: //g' | awk '{print $1, $2}' > $AVG_PATH_LENGTH
    cat $FILE | egrep '^clustering-coefficient:' | sed -e 's/clustering-coefficient: //g' | awk '{print $1, $2}' > $CC
    cat $FILE | egrep '^avg estimation:' | sed -e 's/avg estimation: //g' | awk '{print $1, $2}' > $ESTIMATION
    cat $FILE | egrep '^avg public estimation:' | sed -e 's/avg public estimation: //g' | awk '{print $1, $2}' > $PUBLIC_ESTIMATION
    cat $FILE | egrep '^avg private estimation:' | sed -e 's/avg private estimation: //g' | awk '{print $1, $2}' > $PRIVATE_ESTIMATION
    cat $FILE | egrep '^real avg:' | sed -e 's/real avg: //g' | awk '{print $1, $2}' > $REAL_ESTIMATION

    cat $FILE | egrep '^max public estimation:' | sed -e 's/max public estimation: //g' | awk '{print $1, $2}' > $MAX_PUBLIC_ESTIMATION
    cat $FILE | egrep '^max private estimation:' | sed -e 's/max private estimation: //g' | awk '{print $1, $2}' > $MAX_PRIVATE_ESTIMATION
    cat $FILE | egrep '^min public estimation:' | sed -e 's/min public estimation: //g' | awk '{print $1, $2}' > $MIN_PUBLIC_ESTIMATION
    cat $FILE | egrep '^min private estimation:' | sed -e 's/min private estimation: //g' | awk '{print $1, $2}' > $MIN_PRIVATE_ESTIMATION

    cat $FILE | egrep '^avg estimation error:' | sed -e 's/avg estimation error: //g' | awk '{print $1, $2}' > $AVG_ERROR
    cat $FILE | egrep '^avg public estimation error:' | sed -e 's/avg public estimation error: //g' | awk '{print $1, $2}' > $AVG_PUBLIC_ERROR
    cat $FILE | egrep '^avg private estimation error:' | sed -e 's/avg private estimation error: //g' | awk '{print $1, $2}' > $AVG_PRIVATE_ERROR

    cat $FILE | egrep '^max estimation error:' | sed -e 's/max estimation error: //g' | awk '{print $1, $2}' > $MAX_ERROR
    cat $FILE | egrep '^max public estimation error:' | sed -e 's/max public estimation error: //g' | awk '{print $1, $2}' > $MAX_PUBLIC_ERROR
    cat $FILE | egrep '^max private estimation error:' | sed -e 's/max private estimation error: //g' | awk '{print $1, $2}' > $MAX_PRIVATE_ERROR

    #cat $FILE | egrep '^switched parents counter:' | sed -e "s/switched parents counter: //g" | awk '{print $1, $2}' > $SWITCHED_PARENTS
    #cat $FILE | egrep '^old parents counter:' | sed -e "s/old parents counter: //g" | awk '{print $1, $2}' > $OLD_PARENTS
    #cat $FILE | egrep '^total latency \(s\):' | sed -e "s/total latency (s): //g" | awk '{print $1, $2}' > $LATENCY
    #cat $FILE | egrep '^avg parent rtt:' | sed -e "s/avg parent rtt: //g" | awk '{print $1, $2}' > $RTT
    #cat $FILE | egrep '^not reachable:' | sed -e "s/not reachable: //g" | awk '{print $1, $2}' > $NOT_REACHABLE
    #cat $FILE | egrep '^avg. private nodes load:' | sed -e "s/avg. private nodes load: //g" | awk '{print $1, $2}' > $PRIVATE_LOAD
    #cat $FILE | egrep '^avg. public nodes load:' | sed -e "s/avg. public nodes load: //g" | awk '{print $1, $2}' > $PUBLIC_LOAD

done
