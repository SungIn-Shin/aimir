#!/bin/sh

PID=`/bin/ps -eaf | /bin/grep log4j-fepd`

for pid in $PID
do
	echo "kill -9 $pid"
	kill -9 $pid
done

echo "Complete kill fepd!"

