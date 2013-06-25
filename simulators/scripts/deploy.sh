#!/bin/bash
if [ $# -ne 1 ] ; then
echo "usage: <prog> username"
echo "e.g., ./deploy jdowling"
echo "You must create a dir called 'deploy' on all the cloud machines"
exit 1
fi
prsync -raz -h $1-machines ../deploy/ /home/$1/deploy
