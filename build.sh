#!/bin/bash

mvn clean
mvn install

## Generate docker images
#docker image rm -f pakcatt
#rm docker/*.jar
cp target/*.jar docker/
#cd docker
#docker build -t pakcatt .
#cd ..
#docker images
#docker save pakcatt | gzip > /tmp/pakcatt.tar.gz
#ls -lah /tmp/pakcatt.tar.gz
