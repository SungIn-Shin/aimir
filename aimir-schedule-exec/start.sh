#!/bin/bash

CHK_PARAM=(`which grep | wc`)
if [ $CHK_PARAM -ge 2 ]
then
    LOC_GREP=(`which --skip-alias grep`)
    LOC_AWK=(`which --skip-alias awk`)
else
    LOC_GREP=(`which grep`)
    LOC_AWK=(`which awk`)
fi
PID=(`/bin/ps -eaf | $LOC_GREP java | $LOC_GREP SC | $LOC_AWK '{print $2}'`)
PLEN=${#PID[@]}
MVNREPOSITORY=(`$LOC_GREP localRepository ~/.m2/settings.xml | $LOC_AWK -F "[><]" '{print $3}'`)
if [ "$MVNREPOSITORY" == "" ]
then
    MVNREPOSITORY=~/.m2/repository
fi

mvn -e antrun:run -DscName=SC -DjmxPort=1999 -DspringContext=spring-quartz-soria.xml  2>&1  > /dev/null &
#nohup mvn -e antrun:run -DscName=SC -DjmxPort=1999 -DspringContext=spring-quartz-soria.xml  2>&1 > schedule_debug1.log &
#mvn -e antrun:run -DscName=SC -DjmxPort=1999 -DspringContext=spring-quartz-soria.xml
