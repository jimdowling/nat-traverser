#!/bin/bash

ssh $USER@cloud6.sics.se "cd hpServer ; ./kill.sh "
ssh $USER@cloud7.sics.se "cd hpServer ; ./kill.sh "
