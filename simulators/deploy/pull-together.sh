#!/bin/bash

if [ $# -eq 3 ] ; then
 paste -d " " $1 $2 | cut -d " " -f 1,2,4 > results/$3
 echo "printing output to $3"
elif [ $# -eq 4 ] ; then
 paste -d " " $1 $2 $3 | cut -d " " -f 1,2,4,6 > results/$4
 echo "printing output to $4"
elif [ $# -eq 5 ] ; then
 paste -d " " $1 $2 $3 $4 | cut -d " " -f 1,2,4,6,8 > results/$5
 echo "printing output to $5"
elif [ $# -eq 6 ] ; then
 paste -d " " $1 $2 $3 $4 $5 | cut -d " " -f 1,2,4,6,8,10 > results/$6
 echo "printing output to $6"
elif [ $# -eq 7 ] ; then
 paste -d " " $1 $2 $3 $4 $5 $6 | cut -d " " -f 1,2,4,6,8,10,12 > results/$7
 echo "printing output to $7"
elif [ $# -eq 8 ] ; then
 paste -d " " $1 $2 $3 $4 $5 $6 $7 | cut -d " " -f 1,2,4,6,8,10,12,14 > results/$8
 echo "printing output to $8"
else
 echo "usage: <prog> file1 file2 ..  outputFile"
fi

echo "Exiting..."
exit 0
