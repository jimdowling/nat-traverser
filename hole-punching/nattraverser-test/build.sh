#!/bin/bash
set -e
mvn assembly:assembly -DskipTests
cp target/nattraverser-test-1.0-SNAPSHOT-jar-with-dependencies.jar deploy/hp.jar

