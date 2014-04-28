#!/bin/bash
/home/jdowling/.netbeans/7.0/maven/bin/mvn -Dexec.classpathScope=runtime "-Dexec.args=-Xdebug  -classpath %classpath se.sics.gvod.simulator.croupier.main.Main 13 2000"  -Dexec.executable=/usr/lib/jvm/default-java/bin/java process-classes org.codehaus.mojo:exec-maven-plugin:1.2:exec -o > blah
