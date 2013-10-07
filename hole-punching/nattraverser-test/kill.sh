#!/bin/bash

for i in 1 2 3 4 5 6 7
do
if [ $i -ne 6 ] ; then
  ssh $USER@cloud$i.sics.se "cd hpServer ; ./kill.sh ; killall java"
else 
  ssh $USER@cloud$i.sics.se "cd hpServer ; ./kill.sh "
fi
done

