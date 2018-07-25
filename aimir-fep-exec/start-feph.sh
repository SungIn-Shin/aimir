#!/bin/sh
mvn -e -f pom-feph.xml antrun:run -DfepName=FEP1 -Dif4Port=8000 -DniTcpPort=8001 -DniPanaPort=8004 -DcommandPort=8900 -DjmxPort=1099 2>&1 > feph.log &
