#!/bin/bash
./kill.sh
set -e
./build.sh
./copy.sh
./start.sh
