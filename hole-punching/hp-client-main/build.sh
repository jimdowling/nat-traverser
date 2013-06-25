#!/bin/bash
mvn assembly:assembly -o
cp target/hp-client-main-1.0-SNAPSHOT-jar-with-dependencies.jar deploy/hp.jar

