#!/bin/bash
mvn assembly:assembly
#mv target/hp-server-main-0.4.2.7-SNAPSHOT-jar-with-dependencies.jar target/hpserver.jar
scp target/hp-server-main-0.4.2.7-SNAPSHOT-jar-with-dependencies.jar salman@kajsa.sics.se:/home/salman/hpServer.jar
scp target/hp-server-main-0.4.2.7-SNAPSHOT-jar-with-dependencies.jar salman@lucan.sics.se:/home/salman/hpServer.jar
