#!/bin/sh 
mvn -e -f pom-fepd.xml antrun:run -DfepName=FEP1 -DjmxPort=1199 2>&1 & 
