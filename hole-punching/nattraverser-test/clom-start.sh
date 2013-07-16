#!/bin/bash

ssh root@109.69.12.17 "cd hpServer ; killall java ; ./run.sh false 3 0 109.69.8.203 true"
ssh root@109.69.8.203 "cd hpServer ; killall java ; ./run.sh false 1 0 109.69.12.17 true"

echo ""
echo "See log file: root@109.69.12.17  ~/hpServer"
echo "See log file: root@109.69.8.203  ~/hpServer"
echo ""
