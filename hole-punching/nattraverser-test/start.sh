#!/bin/bash

declare -a hosts=(cloud4 cloud5)

if [ ${#hosts[@]} -lt 2 ] ; then
 echo "You must define more than 1 host."
else 
 echo "${hosts[@]}"
fi

x=1
for i in ${hosts[@]}
do
if [ $x -eq ${#hosts[@]} ] ; then
 y=0
else
 y=$x
fi

 ssh $USER@$i.sics.se "cd hpServer ; killall java ; ./run.sh false 3 0 ${hosts[$y]}.sics.se true"
 echo "ssh $USER@$i.sics.se 'cd hpServer ; killall java ; ./run.sh false 3 0 ${hosts[$y]}.sics.se true'"
x=`expr $x + 1`
done


#ssh $USER@cloud5.sics.se "cd hpServer ; killall java ; ./run.sh false 3 0 cloud4.sics.se true"
#ssh $USER@cloud4.sics.se "cd hpServer ; killall java ; ./run.sh false 1 0 cloud5.sics.se true"

echo ""
echo "See log file: $USER@cloud5.sics.se ~/hpServer"
echo "See log file: $USER@cloud4.sics.se ~/hpServer"
echo ""
