#!/bin/bash 

if [ $# -lt 6 ] ; then
echo "usage: <prog> filename xlabel ylabel xrange yrange charttitle title1 [title2 title3 title4 title5 title6]"
echo "e.g., range = 100"
exit
fi
filename=$1 
xaxis=$2
yaxis=$3
xlen=$4
ystart=$5
ylen=$6
rubrik1=$7
rubrik2=$8
rubrik3=$9
rubrik4=${10}
rubrik5=${11}
rubrik6=${12}
rubrik7=${13}

export filename
export rubrik1
export rubrik2
export rubrik3
export rubrik4
export rubrik5
export rubrik6
export rubrik7
export xaxis
export yaxis
export xlen
export ylen
export ystart

if [ "$rubrik3" = "" ] ; then
  ./gplot2.plt
elif [ "$rubrik4" = "" ] ; then
  ./gplot3.plt
elif [ "$rubrik5" = "" ] ; then
  ./gplot4.plt
elif [ "$rubrik6" = "" ] ; then
  ./gplot5.plt
elif [ "$rubrik7" = "" ] ; then
  ./gplot6.plt
else
  ./gplot7.plt
fi

exit
