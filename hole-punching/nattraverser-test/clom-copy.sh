#!/bin/bash
parallel-rsync -raz -h clom-machines ./deploy/ /root/hpServer
