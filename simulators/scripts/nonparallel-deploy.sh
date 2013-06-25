#!/bin/bash
if [ $# -ne 1 ] ; then
echo "usage: <prog> username"
echo "e.g., ./deploy jdowling"
echo "You must create a dir called 'deploy' on all the cloud machines"
exit 1
fi
for machine in $(awk '{printf "%s ",$1}' nwahlen-machines)
do
    echo Deploying on $machine
    rsync -raz ../deploy/ $1@$machine:/home/$1/deploy
done
date "+%Y-%m-%d %H:%M:%S"
