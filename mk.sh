#!/bin/bash
BUILD_ACTIONS=0
BUILD_CONFIGURER=0
BUILD_BALANCER=0
BUILD_INITDB=0

if [ "${BUILD_ACTIONS}" == "1" ]; then
    echo "### build actions"
    docker build -t abatalev/actions:2021-09-26 -f Dockerfile.actions .
    if [  $? -ne 0  ]; then exit 1; fi
fi
if [ "${BUILD_CONFIGURER}" == "1" ]; then
    echo "### build configurer"
    docker build -t abatalev/configurer:2021-09-26 -f Dockerfile.configurer .
    if [  $? -ne 0  ]; then exit 1; fi;
fi
if [ "${BUILD_BALANCER}" == "1" ]; then
    echo "### build balancer"
    docker build -t abatalev/balancer:2021-09-26 -f Dockerfile.balancer .
    if [  $? -ne 0  ]; then exit 1; fi;
fi
if [ "${BUILD_INITDB}" == "1" ]; then
    echo "### build initdb"
    docker build -t abatalev/initdb:2021-09-26 -f Dockerfile.initdb .
    if [  $? -ne 0  ]; then exit 1; fi;
fi
echo "### launch docker-compose"
docker-compose up # --scale actions=2