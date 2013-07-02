#!/bin/bash
set -e
./kill.sh
./build.sh
./copy.sh
./start.sh
