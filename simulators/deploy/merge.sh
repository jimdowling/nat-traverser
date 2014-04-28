#!/bin/bash

exp=`cat experiment`
echo "Output files: results/$exp"

c=0
declare -a dataDirs

for x in `cat seeds`
do
dataDirs[$c]="unprocessed/$x"
c=`expr $c + 1`
done

#element_count=${#colors[@]}

for E in estimation public-estimation private-estimation avg-err avg-pub-err avg-priv-err real-distribution num-nodes
do
F=""
nos="1,"
index=0
cols="\$1,("
while [ "$index" -lt "$c" ]
do
  idx=`expr $index \* 2`
  idx=`expr $idx + 2`
  if [ `expr $index + 1` -eq "$c" ] ; then
    nos="$nos$idx"
    cols="$cols\$`expr $index + 2`)/$c"
  else
    cols="$cols\$`expr $index + 2`+"
    nos="$nos$idx,"
  fi
  fx=
  F="$F ${dataDirs[$index]}/$E"
  index=`expr $index + 1`
done
#echo "files are: $F"
#echo "nos are: $nos"
#echo "cols are: $cols"
if [ ! -d results/$exp ] ; then
 mkdir results/$exp
fi
#echo "paste -d \" \" $F | cut -d \" \" -f $nos > results/$exp/$E"
paste -d " " $F | cut -d " " -f $nos > results/$exp/$E
awk '{ tot=0; for (i=2; i<=NF; i++) tot += $i; print $1, tot/(NF-1); }' results/$exp/$E > results/$exp/$E.avg
done


for E in max-err max-pub-err max-priv-err max-pub-estimation max-priv-estimation 
do
F=""
nos="1,"
index=0
cols="\$1,("
while [ "$index" -lt "$c" ]
do
  idx=`expr $index \* 2`
  idx=`expr $idx + 2`
  if [ `expr $index + 1` -eq "$c" ] ; then
    nos="$nos$idx"
    cols="$cols\$`expr $index + 2`)/$c"
  else
    cols="$cols\$`expr $index + 2`+"
    nos="$nos$idx,"
  fi
  fx=
  F="$F ${dataDirs[$index]}/$E"
  index=`expr $index + 1`
done
#echo "files are: $F"
#echo "nos are: $nos"
#echo "cols are: $cols"

paste -d " " $F | cut -d " " -f $nos > results/$exp/$E

awk '{ max=0; for (i=2; i<=NF; i++) if ($i>max) max=$i; print $1, max; }' results/$exp/$E > results/$exp/$E.max
done



for E in  min-pub-estimation min-priv-estimation
do
F=""
nos="1,"
index=0
cols="\$1,("
while [ "$index" -lt "$c" ]
do
  idx=`expr $index \* 2`
  idx=`expr $idx + 2`
  if [ `expr $index + 1` -eq "$c" ] ; then
    nos="$nos$idx"
    cols="$cols\$`expr $index + 2`)/$c"
  else
    cols="$cols\$`expr $index + 2`+"
    nos="$nos$idx,"
  fi
  fx=
  F="$F ${dataDirs[$index]}/$E"
  index=`expr $index + 1`
done
#echo "files are: $F"
#echo "nos are: $nos"
#echo "cols are: $cols"

paste -d " " $F | cut -d " " -f $nos > results/$exp/$E

awk '{ min=100000; for (i=2; i<=NF; i++) if ($i<min) min=$i; print $1, min; }' results/$exp/$E > results/$exp/$E.min

done


echo "Results output to results/$exp files"
exit 0
