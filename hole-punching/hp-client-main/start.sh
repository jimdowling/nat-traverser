#!/bin/bash
#for i in 1 2 3 4 5 6 7
#do
#ssh $USER@cloud$i.sics.se "cd hpServer ; killall java ; ./run.sh false 3478 cloud6.sics.se"
#done

ssh $USER@cloud7.sics.se "cd hpServer ; ./kill.sh ; ./run.sh false 1 cloud6.sics.se true"
ssh $USER@cloud6.sics.se "cd hpServer ; ./kill.sh ; ./run.sh false 2 cloud7.sics.se true"

echo ""
echo "See log file: $USER@cloud7.sics.se ~/hpServer"
echo ""
