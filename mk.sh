#!/bin/bash
B_ACTIONS=1
B_CONFIGURER=1
B_BALANCER=1
B_INITDB=1

I_GROUP="abatalev"
I_ACTIONS="${I_GROUP}/actions"
I_BALANCER="${I_GROUP}/balancer"
I_CONFIGURER="${I_GROUP}/configurer"
I_INITDB="${I_GROUP}/initdb"

V_ACTIONS="2021-10-28"
V_CONFIGURER="2021-10-28"
V_BALANCER="2021-10-28"
V_INITDB="2021-10-28"

function image_exist() {
    docker image list | grep ${1} | awk ' {print $2;}' | grep ${2} | wc -l
}

if [ -f "prj2hash" ]; then
    V_ACTIONS=$(./prj2hash -short build/actions)
    V_CONFIGURER=$(./prj2hash -short build/configurer)
    V_BALANCER=$(./prj2hash -short build/balancer)
    V_INITDB=$(./prj2hash -short build/initdb)

    B_ACTIONS=$(image_exist $I_ACTIONS $V_ACTIONS)
    B_CONFIGURER=$(image_exist $I_CONFIGURER $V_CONFIGURER)
    B_BALANCER=$(image_exist $I_BALANCER $V_BALANCER)
    B_INITDB=$(image_exist $I_INITDB $V_INITDB)
fi

IV_ACTIONS=$I_ACTIONS:$V_ACTIONS
IV_BALANCER=$I_BALANCER:$V_BALANCER
IV_CONFIGURER=$I_CONFIGURER:$V_CONFIGURER
IV_INITDB=$I_INITDB:$V_INITDB

CDIR=$(pwd)
cd "${CDIR}/build"
if [ ${B_ACTIONS} == 0 ]; then
    echo "### build actions"
    docker build -t $IV_ACTIONS -f Dockerfile.actions .
    if [  $? -ne 0  ]; then exit 1; fi
else 
   echo "### build actions skipped.    $V_ACTIONS"    
fi
if [ ${B_CONFIGURER} == 0 ]; then
    echo "### build configurer"
    docker build -t $IV_CONFIGURER -f Dockerfile.configurer .
    if [  $? -ne 0  ]; then exit 1; fi;
else 
   echo "### build configurer skipped. $V_CONFIGURER"    
fi
if [ ${B_BALANCER} == 0 ]; then
    echo "### build balancer"
    docker build -t $IV_BALANCER -f Dockerfile.balancer .
    if [  $? -ne 0  ]; then exit 1; fi;
else 
   echo "### build balancer skipped.   $V_BALANCER"    
fi
if [ ${B_INITDB} == 0 ]; then
    echo "### build initdb"
    docker build -t $IV_INITDB -f Dockerfile.initdb .
    if [  $? -ne 0  ]; then exit 1; fi;
else 
   echo "### build initdb skipped.     $V_INITDB"    
fi

cd "${CDIR}"
echo "### create docker-compose"
cp docker-compose.tmpl docker-compose.yaml
IMAGE_VERSION=$IV_ACTIONS yq e -i '.services.actions.image = strenv(IMAGE_VERSION)' docker-compose.yaml
IMAGE_VERSION=$IV_BALANCER yq e -i '.services.balancer.image = strenv(IMAGE_VERSION)' docker-compose.yaml
IMAGE_VERSION=$IV_CONFIGURER yq e -i '.services.configurer.image = strenv(IMAGE_VERSION)' docker-compose.yaml
IMAGE_VERSION=$IV_INITDB yq e -i '.services.init.image = strenv(IMAGE_VERSION)' docker-compose.yaml

echo "### launch docker-compose"
docker-compose up # --scale actions=2