#!/bin/bash
#for i in 1 2 3 4 5 6 7
#do
#ssh $USER@cloud5.sics.se "cd hpServer ; killall java ; ./run.sh false 3 0 cloud7.sics.se true"
#ssh $USER@cloud7.sics.se "cd hpServer ; killall java ; ./run.sh false 1 0 cloud5.sics.se true"
ssh $USER@cloud7.sics.se "cd hpServer ; killall java ; ./run.sh false 1 0 109.69.8.203 true"
ssh root@109.69.12.17 "cd hpServer ; killall java ; ./run.sh false 3 0 cloud7.sics.se true"

#done

echo ""
echo "See log file: $USER@cloud5.sics.se ~/hpServer"
echo "See log file: $USER@cloud7.sics.se ~/hpServer"
echo ""
