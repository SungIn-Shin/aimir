#!/bin/sh
cd /home/aimir/aimiramm/aimir-mm-ecg
./stop.sh
mvn -e antrun:run -Dname=MM -DjmxPort=1400 2>&1 > /dev/null &
