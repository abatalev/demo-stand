#!/bin/bash

PRJ_IMAGEGROUP="abatalev"
PRJ_IMAGEVERSION="2021-10-28"
PRJ_MODULES="actions balancer configurer initdb"
CDIR=$(pwd)

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

function build_project() {
    PRJ_NAME=$1
    PRJ_NEEDBUILD=1
    PRJ_IMAGE="${PRJ_IMAGEGROUP}/${PRJ_NAME}"
    PRJ_VERSION="${PRJ_IMAGEVERSION}"
    
    cd "${CDIR}"
    if [ -f "prj2hash" ]; then
        PRJ_VERSION=$(./prj2hash -short build/${PRJ_NAME})
        PRJ_NEEDBUILD=$(image_exist $PRJ_IMAGE $PRJ_VERSION)
    fi
    cd "${CDIR}/build"
    image_build $PRJ_NEEDBUILD $PRJ_IMAGE $PRJ_VERSION "Dockerfile.${PRJ_NAME}" "${PRJ_NAME}"
    cd "${CDIR}"
    IMAGE_VERSION=$PRJ_IMAGE:$PRJ_VERSION yq e -i ".services.${PRJ_NAME}.image = strenv(IMAGE_VERSION)" docker-compose.yaml
}

function build_all() {
    echo "### create docker-compose"
    cp docker-compose.tmpl docker-compose.yaml
    for prj_name in ${1}
    do
        build_project "${prj_name}"
    done

    cd "${CDIR}"
    echo "### launch docker-compose"
    docker-compose up # --scale actions=2
}

build_all "${PRJ_MODULES}"