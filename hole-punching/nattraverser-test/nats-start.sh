#!/bin/bash

declare -a hosts=(1 2 3 6 7)
declare -a nats=(
                 'NAT_EI_PP_PD'
                 'NAT_EI_PC_EI'
                 'NAT_EI_RD_PD'
                 'NAT_EI_RD_EI'
                 'NAT_EI_PP_HD'
                )

if [ ${#hosts[@]} -lt 2 ] ; then
 echo "You must define more than 1 host."
else 
 echo "${hosts[@]}"
fi

for i in ${hosts[@]}
do
 ssh $USER@cloud$i.sics.se "cd hpServer ; ./kill.sh "
done


x=0
for i in ${hosts[@]}
do
 ssh $USER@cloud$i.sics.se "cd hpServer ; ./run.sh false $i 0 4@cloud4.sics.se false ${nats[$x]}"
 echo "Starting on cloud$i with nat ${nats[$x]}"
 x=`expr $x + 1`
 sleep 5
done


echo ""
