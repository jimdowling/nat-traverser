#!/bin/bash

# In NetBeans, do ctrl-g to select "Nat Type"
# Dump output to file that is parsed here to discover the ratio of actual nats to discovered nats

echo "Parses a file to discover the ratio of actual nats to discovered nats"
echo "usage: <prog> filename"

for nt in m\(EI\)_a\(PP\)_f\(PD\) m\(EI\)_a\(PP\)_f\(EI\) m\(PD\)_a\(RD\)_f\(PD\) m\(EI\)_a\(RD\)_f\(PD\) m\(EI\)_a\(RD\)_f\(EI\) m\(EI\)_a\(PP\)_f\(HD\) m\(EI\)_a\(PC\)_f\(EI\) m\(PD\)_a\(PP\)_f\(PD\) m\(PD\)_a\(PP\)_f\(EI\) m\(PD\)_a\(RD\)_f\(EI\) m\(EI\)_a\(PC\)_f\(PD\) m\(EI\)_a\(RD\)_f\(HD\) m\(HD\)_a\(RD\)_f\(PD\)
do

c1=`grep "Nat Type is) : ${nt}" $1 | wc -l`
c2=`grep "is SUCCEED - ${nt}" $1 | wc -l`
#c2=`grep "Nat Type is ${nt}" $1 | wc -l`

echo "$nt  - $c1/$c2  (actual/identified)"
done
