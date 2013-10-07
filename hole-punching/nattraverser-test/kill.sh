#!/bin/bash

for i in 1 2 3 4 5 6 7
do
ssh $USER@cloud$i.sics.se "cd hpServer ; ./kill.sh"
done

