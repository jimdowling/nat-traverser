#!/bin/bash
mvn exec:java -Dexec.mainClass=se.sics.kompics.nat.hp.servermain.HPServerMain -Dexec.args="lucan.sics.se 0"
