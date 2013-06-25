#!/usr/bin/gnuplot -persist 

filename=system("echo $filename")
rubrik1=system("echo $rubrik1")
rubrik2=system("echo $rubrik2")
rubrik3=system("echo $rubrik3")
rubrik4=system("echo $rubrik4")
rubrik5=system("echo $rubrik5")
xaxis=system("echo $xaxis")
yaxis=system("echo $yaxis")
xlen=system("echo $xlen")
ylen=system("echo $ylen")
ystart=system("echo $ystart")

set title rubrik1

#  set style line
# Increase sampling accuracy
#  set isosample 80,80;
# Draw only the surface 
#  set hidden3d;
# Draw contours
  set contour base;
  
set xlabel xaxis; set ylabel yaxis; 


set xrange [0:xlen]
set yrange [ystart:ylen]
set logscale y


set arrow 1 from 61,ystart to 61,ylen nohead 

filedata(n)=sprintf("results/%s",n)
b=filedata(filename)
ival=10
plot b using 1:2 title rubrik2 with linespoints pointtype 1 lw 3 pointinterval 1000\
 ,b using 1:3 title rubrik3 with linespoints pointtype 2 lw 3 pointinterval ival\
 ,b using 1:4 title rubrik4 with linespoints pointtype 3 lw 3 pointinterval ival\
 ,b using 1:5 title rubrik5 with linespoints pointtype 4 lw 3 pointinterval ival\
;

set terminal post eps enhanced color; # png color
figout(n)=sprintf("figs/%s.eps",n)
set output figout(filename);
replot;

# We can save all settings and how gnuplot ran
#  save "push.plt"

#pause -1 
quit
