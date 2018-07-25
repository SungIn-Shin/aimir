#!/bin/sh
mvn -e -f pom-fepa.xml antrun:run -DfepName=FEP1 -DauthTcpPort=9001 -DjmxPort=1299 2>&1 &
