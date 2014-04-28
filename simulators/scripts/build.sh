#!/bin/bash
cd ..
mvn assembly:assembly 
echo "Moving jar file to: ../deploy/gvod-simulator.jar"
mv target/gvod-simulator-1.0-SNAPSHOT-jar-with-dependencies.jar  deploy/gvod-simulator.jar
cp src/main/resources/log4j.properties deploy/