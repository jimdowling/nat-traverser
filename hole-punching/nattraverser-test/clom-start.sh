#!/bin/bash

ssh root@10.138.15.133 "cd hpServer ; killall java ; ./run.sh false 1 1 2@10.139.40.10 true"
ssh root@10.139.40.10 "cd hpServer ; killall java ; ./run.sh false 2 1 1@10.138.15.133 true"

echo ""
echo "See log file: root@109.69.12.17  ~/hpServer"
echo "See log file: root@109.69.8.203  ~/hpServer"
echo ""
