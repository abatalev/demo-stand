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

function image_build() {
    B_PRJ=$1
    I_PRJ=$2
    V_PRJ=$3
    D_PRJ=$4
    N_PRJ=$5
    if [ ${B_PRJ} == 0 ]; then
        echo "### build $N_PRJ"
        docker build -t $I_PRJ:$V_PRJ -f $D_PRJ .
        if [  $? -ne 0  ]; then exit 1; fi
    else 
        echo "### Build $N_PRJ skipped. Image $I_PRJ:$V_PRJ already exist"    
    fi
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

CDIR=$(pwd)
cd "${CDIR}/build"
image_build $B_ACTIONS $I_ACTIONS $V_ACTIONS Dockerfile.actions "actions"
image_build $B_CONFIGURER $I_CONFIGURER $V_CONFIGURER Dockerfile.configurer "configurer"
image_build $B_BALANCER $I_BALANCER $V_BALANCER Dockerfile.balancer "balancer"
image_build $B_INITDB $I_INITDB $V_INITDB Dockerfile.initdb "initdb"

cd "${CDIR}"
echo "### create docker-compose"
cp docker-compose.tmpl docker-compose.yaml
IMAGE_VERSION=$I_ACTIONS:$V_ACTIONS yq e -i '.services.actions.image = strenv(IMAGE_VERSION)' docker-compose.yaml
IMAGE_VERSION=$I_BALANCER:$V_BALANCER yq e -i '.services.balancer.image = strenv(IMAGE_VERSION)' docker-compose.yaml
IMAGE_VERSION=$I_CONFIGURER:$V_CONFIGURER yq e -i '.services.configurer.image = strenv(IMAGE_VERSION)' docker-compose.yaml
IMAGE_VERSION=$I_INITDB:$V_INITDB yq e -i '.services.init.image = strenv(IMAGE_VERSION)' docker-compose.yaml

echo "### launch docker-compose"
docker-compose up # --scale actions=2