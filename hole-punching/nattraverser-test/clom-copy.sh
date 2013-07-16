#!/bin/bash
parallel-rsync -raz -h machines ./deploy/ /root/hpServer
