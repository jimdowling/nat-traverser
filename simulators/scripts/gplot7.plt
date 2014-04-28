#!/usr/bin/gnuplot -persist 

filename=system("echo $filename")
rubrik1=system("echo $rubrik1")
rubrik2=system("echo $rubrik2")
rubrik3=system("echo $rubrik3")
rubrik4=system("echo $rubrik4")
rubrik5=system("echo $rubrik5")
rubrik6=system("echo $rubrik6")
rubrik7=system("echo $rubrik7")
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

filedata(n)=sprintf("results/%s",n)
b=filedata(filename)
ival=10
plot b using 1:2 title rubrik2 with linespoints pointtype 1 lw 3 pointinterval ival\
 ,b using 1:3 title rubrik3 with linespoints pointtype 2 lw 3 pointinterval ival\
 ,b using 1:4 title rubrik4 with linespoints pointtype 3 lw 3 pointinterval ival\
 ,b using 1:5 title rubrik5 with linespoints pointtype 4 lw 3 pointinterval ival\
 ,b using 1:6 title rubrik6 with linespoints pointtype 5 lw 3 pointinterval ival\
 ,b using 1:7 title rubrik7 with linespoints pointtype 6 lw 3 pointinterval ival\
;

#set style line 1 lt 1 lc rgb "green" lw 3
#set style line 3 lt 3 lc rgb "red" lw 3
#set style line 4 lt 4 lc rgb "blue" lw 3
#set style line 6 lt 6 lc rgb "violet" lw 3
#set style line 8 lt 8 lc rgb "orange" lw 3
#set style line 12 lt 12 lc rgb "royalblue" lw 3

set terminal post eps enh colour; # png color
figout(n)=sprintf("figs/%s.eps",n)
set output figout(filename);
replot;

# We can save all settings and how gnuplot ran
#  save "push.plt"

#pause -1 
quit
