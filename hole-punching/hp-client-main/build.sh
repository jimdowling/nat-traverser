#!/bin/bash
mvn assembly:assembly -DskipTests
cp target/hp-client-main-1.0-SNAPSHOT-jar-with-dependencies.jar deploy/hp.jar

