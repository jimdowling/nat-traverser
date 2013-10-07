#!/bin/bash

declare -a hosts=(4 5)

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
 ssh $USER@cloud$i.sics.se "cd hpServer ; killall java ; ./run.sh false $i 0 ${hosts[$y]}@cloud${hosts[$y]}.sics.se true"
 echo "ssh $USER@cloud$i.sics.se 'cd hpServer ; killall java ; ./run.sh false $i 0 ${hosts[$y]}@cloud${hosts[$y]}.sics.se true'"
 x=`expr $x + 1`
done


echo ""
