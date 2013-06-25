#!/bin/bash
mvn assembly:assembly
mv target/hp-server-main-0.4.2.7-SNAPSHOT-jar-with-dependencies.jar ./hpserver.jar
scp hpserver.jar jdowling@cloud1.sics.se:~/
scp hpserver.jar jdowling@lucan.sics.se:~/
