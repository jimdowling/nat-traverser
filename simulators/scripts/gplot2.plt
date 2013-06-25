#!/usr/bin/gnuplot -persist 

filename=system("echo $filename")
rubrik1=system("echo $rubrik1")
rubrik2=system("echo $rubrik2")
xaxis=system("echo $xaxis")
yaxis=system("echo $yaxis")
xlen=system("echo $xlen")
ylen=system("echo $ylen")

set title rubrik1

#  set style line
# Increase sampling accuracy
#  set isosample 80,80;
# Draw only the surface 
#  set hidden3d;
# Draw contours
#  set contour base;
  
set xlabel xaxis; set ylabel yaxis; 

set xrange [0:xlen]
set yrange [0:ylen]

filedata(n)=sprintf("results/%s",n)
b=filedata(filename)
plot b using 1:2 title rubrik2 with lines \
;

set terminal post eps color; # png 
figout(n)=sprintf("figs/%s.eps",n)
set output figout(filename);
replot;

# We can save all settings and how gnuplot ran
#  save "push.plt"

#pause -1 
quit
