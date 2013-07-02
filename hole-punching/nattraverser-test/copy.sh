#!/bin/bash
parallel-rsync -raz -h machines ./deploy/ ${HOME}/hpServer
